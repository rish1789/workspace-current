package com.example.myapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.ErrorException.DuplicateResponseException;
import com.example.myapp.ErrorException.ResourceNotFoundException;
import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.AddWorkspaceMemberRequest;
import com.example.myapp.dto.request.CreateWorkspaceRequest;
import com.example.myapp.dto.request.TransferOwnershipRequest;
import com.example.myapp.dto.request.UpdateWorkspaceDescriptionRequest;
import com.example.myapp.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.example.myapp.dto.request.UpdateWorkspaceNameRequest;
import com.example.myapp.dto.respond. WorkspaceMemberResponse;
import com.example.myapp.dto.respond. WorkspaceResponse;
import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Board;
import com.example.myapp.entity.Workspace;
import com.example.myapp.entity.WorkspaceMember;
import com.example.myapp.entity.WorkspaceMember.Role;
import com.example.myapp.repository.ActivityLogRepository;
import com.example.myapp.repository.BoardMemberRepository;
import com.example.myapp.repository.BoardRepository;
import com.example.myapp.repository.CardLabelRepository;
import com.example.myapp.repository.CardMemberRepository;
import com.example.myapp.repository.CardRepository;
import com.example.myapp.repository.ChecklistItemRepository;
import com.example.myapp.repository.ChecklistRepository;
import com.example.myapp.repository.CommentRepository;
import com.example.myapp.repository.LabelRepository;
import com.example.myapp.repository.LaneRepository;
import com.example.myapp.repository.WorkspaceMemberRepository;
import com.example.myapp.repository.WorkspaceRepository;

/**
 * Service class responsible for all Workspace-related business logic.
 *
 * Responsibilities:
 *  - Creates, retrieves, updates, and deletes workspaces
 *  - Manages workspace membership (add, remove, update role)
 *  - Automatically adds the creator as ADMIN when a workspace is created
 *  - Deleting a workspace cascades to boards, lanes, members, and labels
 *
 * Dependencies:
 *  - WorkspaceRepository       — persists and retrieves workspaces
 *  - WorkspaceMemberRepository — persists and retrieves workspace members
 *  - BoardRepository           — finds boards to delete on workspace delete
 *  - BoardMemberRepository     — deletes board members on workspace delete
 *  - LabelRepository           — deletes labels on workspace delete
 *  - LaneRepository            — deletes lanes on workspace delete
 *  - UserRepository            — validates user existence
 */
@Service
@Transactional
public class WorkspaceService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────

    private final WorkspaceRepository       workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final BoardRepository           boardRepository;
    private final Helper                    helper;
    private final AccessControl             accessControl;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository memberRepository,
                            BoardRepository boardRepository,
                            BoardMemberRepository boardMemberRepository,
                            LabelRepository labelRepository,
                            LaneRepository laneRepository,
                            CardRepository cardRepository,
                            CardMemberRepository cardMemberRepository,
                            CardLabelRepository cardLabelRepository,
                            CommentRepository commentRepository,
                            ChecklistRepository checklistRepository,
                            ChecklistItemRepository checklistItemRepository,
                            ActivityLogRepository activityLogRepository,
                            Helper helper, AccessControl accessControl) {


        this.workspaceRepository  = workspaceRepository;
        this.memberRepository     = memberRepository;
        this.boardRepository      = boardRepository;
        this.helper               = helper;
        this.accessControl        = accessControl;
    }

    

    

    private  WorkspaceResponse toRespond(Workspace workspace) {
    return new  WorkspaceResponse(
        workspace.getId(),
        workspace.getName(),
        workspace.getDescription(),
        workspace.getCreatedBy().getId(),
        workspace.getCreatedAt()
    );
    }

    private  WorkspaceMemberResponse toMemberRespond(WorkspaceMember m) {
    return new  WorkspaceMemberResponse(
        m.getWorkspace().getId(),
        m.getUser().getId(),
        m.getUser().getUsername(),
        m.getRole(),
        m.getJoinedAt()
    );
    }


    // ─── WORKSPACE CRUD ──────────────────────────────────────────────────────

    /**
     * Creates a new workspace and automatically adds creator as ADMIN.
     */
    public  WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        AppUser   createdBy = helper.getCurrentUser();
        if(workspaceRepository.existsByNameAndCreatedBy(request.getWorkspaceName(), createdBy)){
            throw new DuplicateResponseException("Workspace with name : "+request.getWorkspaceName()+" is already created");
        }
        Workspace workspace = workspaceRepository.save(new Workspace(request.getWorkspaceName(), createdBy));
        memberRepository.save(new WorkspaceMember(workspace, createdBy, Role.OWNER));
        return toRespond(workspace);
    }

   // Get all workspace created by user from userId

    public List< WorkspaceResponse> getAllWorkspace(){
       AppUser user = helper.getCurrentUser();
       return memberRepository.findByUser(user).stream()
            .map(m -> toRespond(m.getWorkspace()))
            .toList();
    }
    /**
     * Retrieves a workspace by its ID.
     */
    public  WorkspaceResponse getWorkspaceById(Integer wrkspId) {
        Workspace workspace =  helper.resolveWorkspace(wrkspId);
        AppUser user =helper.getCurrentUser();
        accessControl.requiredWorkspaceMember(workspace, user);
        return toRespond(workspace);
    }

    /**
     * Updates the workspace name.
     */
    public  WorkspaceResponse updateWorkspaceName(Integer wrkspId,UpdateWorkspaceNameRequest request) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser user =helper.getCurrentUser();
        accessControl.requiredWorkspaceAdminOrAbove(workspace, user);
        if(workspaceRepository.existsByNameAndCreatedBy(request.getWorkspaceName(),workspace.getCreatedBy())){
            throw new DuplicateResponseException("Workspace with name : "+request.getWorkspaceName()+" is already created");
        }
        workspace.setName(request.getWorkspaceName());
        return toRespond(workspaceRepository.save(workspace));
        
    }

    /**
     * Updates the workspace description.
     */
    public  WorkspaceResponse updateDescription(Integer wrkspId,UpdateWorkspaceDescriptionRequest request) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser user =helper.getCurrentUser();
        accessControl.requiredWorkspaceAdminOrAbove(workspace, user);
        workspace.setDescription(request.getDescription());
        return toRespond(workspaceRepository.save(workspace));
        
    }

    /**
     * Deletes a workspace and all its boards and their children (lanes, cards
     * and their children, board members, labels) via Helper.deleteBoardAndChildren,
     * then workspace members, then the workspace itself.
     */
    public void deleteWorkspace(Integer wrkspId) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser user =helper.getCurrentUser();
        accessControl.requiredWorkspaceOwner(workspace, user);

        List<Board> boards = boardRepository.findByWorkspace(workspace);
        for (Board board : boards) {
            helper.deleteBoardAndChildren(board);
        }

        // delete workspace members
        memberRepository.deleteAll(memberRepository.findByWorkspace(workspace));

        // delete workspace
        workspaceRepository.delete(workspace);
    }

    // ─── MEMBERSHIP MANAGEMENT ───────────────────────────────────────────────

    /**
     * Adds a new member to a workspace.
     * A user cannot be added twice to the same workspace.
     */
    public  WorkspaceMemberResponse addMember(Integer wrkspId,AddWorkspaceMemberRequest request) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser      member   = helper.resolveUser(request.getUserId());
        AppUser user =helper.getCurrentUser();
        if(request.getRole() == Role.OWNER){
            throw new IllegalArgumentException("Cannot add member with Owner role");
        }
        if(request.getRole() == Role.ADMIN){
            accessControl.requiredWorkspaceOwner(workspace, user);
        }
        else{
            accessControl.requiredWorkspaceAdminOrAbove(workspace, user);
        }
        if (memberRepository.existsByWorkspaceAndUser(workspace, member))
            throw new DuplicateResponseException("User is already a member");

        return toMemberRespond(memberRepository.save(new WorkspaceMember(workspace,member,request.getRole())));
        
    }

    /**
     * Removes a member from a workspace.
     */
    public void removeMember(Integer wrkspId, Integer userId) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser   target = helper.resolveUser(userId);
        AppUser user =helper.getCurrentUser();

        if(user.getId().equals(target.getId())){
            throw new IllegalArgumentException("You cannot remove yourself.");
        }

       WorkspaceMember member = accessControl.resolveWorkspaceMembership(workspace, target, "User not found in workspace");
        if(member.getRole()==Role.OWNER){
            throw new IllegalArgumentException("Cannot remove Owner");
        }
        if(member.getRole()==Role.ADMIN){
            accessControl.requiredWorkspaceOwner(workspace, user);
        }

        else{
            accessControl.requiredWorkspaceAdminOrAbove(workspace, user);
        }
        

        memberRepository.delete(member);
    }

    /**
     * Returns all members of a workspace.
     */
    public List< WorkspaceMemberResponse> getMembers(Integer wrkspId) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser user = helper.getCurrentUser();
        accessControl.requiredWorkspaceMember(workspace, user);
        return memberRepository.findByWorkspace(workspace).stream().map(this::toMemberRespond).toList();
    }

    /**
     * Returns the role of a specific member.
     */
    public Role getMemberRole(Integer wrkspId, Integer userId) {
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.requiredWorkspaceMember(workspace, currentUser);
        
        AppUser   user      = helper.resolveUser(userId);

        return accessControl.resolveWorkspaceMembership(workspace, user,"Member not found").getRole();
    }

    /**
     * Updates the role of an existing workspace member.
     */
    public  WorkspaceMemberResponse updateMemberRole(Integer wrkspId, Integer userId,UpdateWorkspaceMemberRoleRequest request) {
        Workspace workspace   = helper.resolveWorkspace(wrkspId);
        AppUser   user        = helper.getCurrentUser();
        AppUser   target      = helper.resolveUser(userId);
    
        WorkspaceMember member = memberRepository
                .findByWorkspaceAndUser(workspace, target)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        if(member.getRole()==Role.OWNER){
            throw new IllegalArgumentException("Use OwnerTransfer endpoints");
        }
        if(request.getRole()==Role.OWNER){
            throw new IllegalArgumentException("Use OwnerTransfer endpoints");
        }
        if (request.getRole() == Role.ADMIN || member.getRole() == Role.ADMIN){
            accessControl.requiredWorkspaceOwner(workspace, user);
        }    
        else{
            accessControl.requiredWorkspaceAdminOrAbove(workspace, user);
        }

        member.setRole(request.getRole());
        return toMemberRespond(memberRepository.save(member));
        
    }

    public void transferOwnerShip(Integer wrkspId,TransferOwnershipRequest request){
        AppUser user = helper.getCurrentUser();
        Workspace workspace = helper.resolveWorkspace(wrkspId);
        accessControl.requiredWorkspaceOwner(workspace, user);

        if(user.getId().equals(request.getUserId())){
            throw new IllegalArgumentException("Already owner");
        }

        AppUser transfer = helper.resolveUser(request.getUserId());
        WorkspaceMember transferMember =  accessControl.resolveWorkspaceMembership(workspace,transfer,"Member not found");

        
        if(transferMember.getRole()!=Role.ADMIN){
            throw new IllegalArgumentException("New Owner must be Admin");
        }
        WorkspaceMember owner = accessControl.resolveWorkspaceMembership(workspace, user);
        owner.setRole(Role.ADMIN);
        transferMember.setRole(Role.OWNER);
          
        memberRepository.save(owner);
        memberRepository.save(transferMember);
        }     
}