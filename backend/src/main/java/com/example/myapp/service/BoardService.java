package com.example.myapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.entity.Board;
import com.example.myapp.entity.BoardMember;

import com.example.myapp.entity.Label;

import com.example.myapp.ErrorException.DuplicateResponseException;

import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.AddBoardMemberRequest;
import com.example.myapp.dto.request.ChangeBoardAdmin;
import com.example.myapp.dto.request.CreateBoardRequest;
import com.example.myapp.dto.request.CreateLabelRequest;
import com.example.myapp.dto.request.UpdateBoardDescriptionRequest;
import com.example.myapp.dto.request.UpdateBoardMemberRoleRequest;
import com.example.myapp.dto.request.UpdateBoardNameRequest;
import com.example.myapp.dto.request.UpdateBoardVisibilityRequest;
import com.example.myapp.dto.respond.BoardMemberResponse;
import com.example.myapp.dto.respond.BoardResponse;
import com.example.myapp.dto.respond.LabelResponse;
import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Workspace;
import com.example.myapp.entity.BoardMember.Role;
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
import com.example.myapp.repository.ActivityLogRepository;
import com.example.myapp.repository.AppUserRepository;
import com.example.myapp.repository.WorkspaceRepository;

/**
 * Service class responsible for all Board-related business logic.
 *
 * Responsibilities:
 *  - Creates, retrieves, updates, and deletes boards
 *  - Manages board membership and labels
 *  - Deleting a board cascades to lanes, cards, members, and labels
 *
 * Dependencies:
 *  - BoardRepository       — persists and retrieves boards
 *  - BoardMemberRepository — persists and retrieves board members
 *  - LabelRepository       — persists and retrieves labels
 *  - LaneRepository        — deletes lanes on board delete
 *  - CardRepository        — deletes cards on board delete
 *  - CardMemberRepository  — deletes card members on board delete
 *  - CardLabelRepository   — deletes card labels on board delete
 *  - UserRepository        — validates user existence
 *  - WorkspaceRepository   — validates workspace existence
 */
@Service
@Transactional
public class BoardService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────

    private final BoardRepository           boardRepository;
    private final BoardMemberRepository     boardMemberRepository;
    private final LabelRepository           labelRepository;    
    private final Helper                    helper;
    private final AccessControl             accessControl;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public BoardService(BoardRepository boardRepository,
                        BoardMemberRepository boardMemberRepository,
                        LabelRepository labelRepository,
                        AppUserRepository userRepository,
                        WorkspaceRepository workspaceRepository,
                        LaneRepository laneRepository,
                        CardRepository cardRepository,
                        CardMemberRepository cardMemberRepository,
                        CardLabelRepository cardLabelRepository,
                        CommentRepository commentRepository,
                        ChecklistRepository checklistRepository,
                        ChecklistItemRepository checklistItemRepository,
                        ActivityLogRepository activityLogRepository,
                        Helper helper, AccessControl accessControl) {
        
        
        this.boardRepository       = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.labelRepository       = labelRepository;
        this.helper                = helper;
        this.accessControl         = accessControl; 
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────


    private BoardResponse toBoardResponse(Board board){
        return new BoardResponse(board.getId(),board.getName(),board.getWorkspace().getId(),
               board.getCreatedBy().getId(),board.getVisibility(),board.getCreatedAt());
    }

    private BoardMemberResponse toBoardMemberResponse(BoardMember member){
        return new BoardMemberResponse(member.getUser().getId(),member.getBoard().getId(),
               member.getUser().getUsername(),member.getRole(),member.getJoinedAt());
    }

    private LabelResponse toLabelResponse(Label label){
        return new LabelResponse(label.getId(),label.getBoard().getId(),label.getLabelName(),label.getColor());
    }
    // ─── BOARD CRUD ──────────────────────────────────────────────────────────

    /**
     * Creates a new board and automatically adds creator as ADMIN.
     */
    public BoardResponse createBoard(CreateBoardRequest request){
        Workspace workspace = helper.resolveWorkspace(request.getWorkspaceId());
        AppUser   user      = helper.getCurrentUser();
        accessControl.requiredWorkspaceAdminOrAbove(workspace, user);
        if(boardRepository.existsByNameAndWorkspace(request.getBoardName(), workspace)){
            throw new DuplicateResponseException("Board exists with same name");
        }
        Board board = boardRepository.save(new Board(workspace,request.getBoardName(),request.getVisibility(), user));
        boardMemberRepository.save(new BoardMember(board, user, Role.ADMIN));
        return toBoardResponse(board);
    }

    /**
     * Retrieves a Board by its ID.
    */
    public BoardResponse getBoardById(Integer boardId) {
    Board board = helper.resolveBoard(boardId);
    accessControl.checkBoardViewAccess(board, helper.getCurrentUser());
    return toBoardResponse(board);
}

    /**
     * Returns all boards in a workspace.
     */
    public List<BoardResponse> getBoardsByWorkspaceId(Integer workspaceId) {
        Workspace workspace = helper.resolveWorkspace(workspaceId);
        accessControl.requiredWorkspaceMember(workspace,helper.getCurrentUser());
        return boardRepository.findByWorkspace(workspace).stream().map(this::toBoardResponse).toList();
    }

    /**
     * Updates the board name.
     */
    public BoardResponse updateBoardName(Integer boardId,UpdateBoardNameRequest request) {
        Board board = helper.resolveBoard(boardId);
        accessControl.requiredBoardAdmin(board,helper.getCurrentUser());
        if(boardRepository.existsByNameAndWorkspace(request.getBoardName(),board.getWorkspace())){
            throw new DuplicateResponseException("Board exists with same name ");
        }
        board.setName(request.getBoardName());
        return toBoardResponse(boardRepository.save(board));
        
    }

    /**
     * Updates the board description.
     */
    public BoardResponse updateDescription(Integer boardId,UpdateBoardDescriptionRequest request) {
        Board board = helper.resolveBoard(boardId);
        accessControl.requiredBoardAdmin(board,helper.getCurrentUser());
        board.setDescription(request.getDescription());
        return toBoardResponse(boardRepository.save(board));
    }

    /**
     * Updates the board visibility.
     */
    public BoardResponse updateVisibility(Integer boardId,UpdateBoardVisibilityRequest request) {
        Board board = helper.resolveBoard(boardId);
        accessControl.requiredBoardAdmin(board,helper.getCurrentUser());
        board.setVisibility(request.getVisibility());
        return toBoardResponse(boardRepository.save(board));

    }

    /**
     * Deletes a board and all its children (lanes, cards and their children,
     * board members, labels) via Helper.deleteBoardAndChildren, then the board itself.
     */
    public void deleteBoard(Integer boardId) {
        Board board = helper.resolveBoard(boardId);
        accessControl.requiredBoardAdmin(board,helper.getCurrentUser());
        helper.deleteBoardAndChildren(board);
    }

    // ─── MEMBERSHIP MANAGEMENT ───────────────────────────────────────────────

    /**
     * Adds a new member to a board.
     */
    public BoardMemberResponse addMember(Integer boardId,AddBoardMemberRequest request) {
        Board board = helper.resolveBoard(boardId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.requiredBoardAdmin(board,currentUser);
        AppUser  user  = helper.resolveUser(request.getUserId());
        if(request.getRole()==Role.ADMIN){
            throw new IllegalArgumentException("Use change admin endpoints to assign admin role");
        }
        if (boardMemberRepository.existsByBoardAndUser(board, user))
            throw new DuplicateResponseException("User is already a member");

        return toBoardMemberResponse(boardMemberRepository.save(new BoardMember(board, user,request.getRole())));
  
    }

    /**
     * Removes a member from a board.
     */
    public void removeMember(Integer boardId, Integer userId) {
        Board board = helper.resolveBoard(boardId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.requiredBoardAdmin(board,currentUser);
        AppUser  user  = helper.resolveUser(userId);
        if(user.getId().equals(currentUser.getId())){
            throw new IllegalArgumentException("You cannot remove yourself");
        }
        BoardMember member = accessControl.resolveBoardMember(board, user);

        boardMemberRepository.delete(member);
    }

    /**
     * Returns all members of a board.
     */
    public List<BoardMemberResponse> getMembersByBoardId(Integer boardId) {
        Board board = helper.resolveBoard(boardId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(board,currentUser);
        return boardMemberRepository.findByBoard(board).stream().map(this::toBoardMemberResponse).toList();
    }

    /**
     * Returns the role of a specific member.
     */
    public Role getMemberRole(Integer boardId, Integer userId) {
        Board    board = helper.resolveBoard(boardId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(board,currentUser);
        AppUser  user  = helper.resolveUser(userId);

        return accessControl.resolveBoardMember(board, user).getRole();
    }

    /**
     * Updates the role of a board member.
     */
    public BoardMemberResponse updateMemberRole(Integer boardId, Integer userId,UpdateBoardMemberRoleRequest request) {
        Board   board = helper.resolveBoard(boardId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.requiredBoardAdmin(board, currentUser);
        AppUser user  = helper.resolveUser(userId);
        if(userId.equals(currentUser.getId())){
            throw new IllegalArgumentException("Use change admin endpoint");
        }
        if(request.getRole()==Role.ADMIN){
            throw new IllegalArgumentException("Use change admin endpoint");
        }
        BoardMember member = accessControl.resolveBoardMember(board, user);
        member.setRole(request.getRole());
        return toBoardMemberResponse(boardMemberRepository.save(member));

    }

    // ─── LABEL MANAGEMENT ────────────────────────────────────────────────────

    /**
     * Creates a new label on a board.
     */
    public LabelResponse createLabel(Integer boardId,CreateLabelRequest request) {
        Board board = helper.resolveBoard(boardId);
        AppUser currentUser = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(board, currentUser);
        if(labelRepository.existsByNameAndBoard(request.getName(), board)){
            throw new DuplicateResponseException("Label name is in use");
        }
        return toLabelResponse(labelRepository.save(new Label(board,request.getName(),request.getColor())));
    }

    /**
     * Retrieves a label by its ID.
     */
    public LabelResponse getLabelById(Integer labelId) {
        Label label = helper.resolveLabel(labelId);
        accessControl.checkBoardViewAccess(label.getBoard(),helper.getCurrentUser());
        return toLabelResponse(label);
    }

    /**
     * Returns all labels on a board.
     */
    public List<LabelResponse> getLabelsByBoardId(Integer boardId) {
        Board board = helper.resolveBoard(boardId);
        accessControl.checkBoardViewAccess(board,helper.getCurrentUser());
        return labelRepository.findByBoard(board).stream().map(this::toLabelResponse).toList();
    }

    /**
     * Deletes a label permanently.
     */
    public void deleteLabel(Integer labelId) {
        Label label = helper.resolveLabel(labelId);
        accessControl.requiredBoardAdmin(label.getBoard(),helper.getCurrentUser());
        labelRepository.delete(label);
    }

    public void changeAdmin(Integer boardId,ChangeBoardAdmin request){
        AppUser current  = helper.getCurrentUser();
        Board board      = helper.resolveBoard(boardId);
        AppUser target  = helper.resolveUser(request.getUserId());
        accessControl.requiredBoardAdmin(board, current);
        if(current.getId().equals(target.getId())){
            throw new IllegalArgumentException("You are already the admin");
        }
        BoardMember newAdmin = accessControl.resolveBoardMember(board, target);
        if(newAdmin.getRole()==Role.OBSERVER){
            throw new IllegalArgumentException("Observer cannot become admin. Only member can become admin");
        }
        BoardMember oldAdmin = accessControl.resolveBoardMember(board,current);
        newAdmin.setRole(Role.ADMIN);
        oldAdmin.setRole(Role.MEMBER);

        boardMemberRepository.save(newAdmin);
        boardMemberRepository.save(oldAdmin);
    }
}