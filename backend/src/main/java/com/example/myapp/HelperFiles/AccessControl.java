package com.example.myapp.HelperFiles;

import org.springframework.stereotype.Component;

import com.example.myapp.ErrorException.AccessDeniedException;
import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Board;
import com.example.myapp.entity.BoardMember;
    
import com.example.myapp.entity.Workspace;
import com.example.myapp.entity.WorkspaceMember;
import com.example.myapp.entity.WorkspaceMember.Role;
import com.example.myapp.repository.BoardMemberRepository;
import com.example.myapp.repository.WorkspaceMemberRepository;

@Component
public class AccessControl {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardMemberRepository     boardMemberRepository;
    private final Helper                    helper;
    public AccessControl(WorkspaceMemberRepository workspaceMemberRepository,
                         BoardMemberRepository boardMemberRepository,
                        Helper helper) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.boardMemberRepository     = boardMemberRepository;
        this.helper                    = helper;
    }

    public void UserAccessAuthentication(AppUser user){
        AppUser current = helper.getCurrentUser();
        if(!current.getId().equals(user.getId())){
            throw new AccessDeniedException("You can only update your own account");
        }
    }
/* 
    public void UserAccessAuthentication(AppUser user,String message){
        AppUser current = helper.getCurrentUser();
        if(!current.getId().equals(user.getId())){
            throw new AccessDeniedException(message);
        }
    }
*/
    // Standardized to 403: whether checking the caller's own access or
    // looking up another member's row, "not a member" means forbidden, not
    // "resource doesn't exist" — consistent with checkBoardViewAccess below.
    public WorkspaceMember resolveWorkspaceMembership(Workspace workspace, AppUser user) {
        return workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));
    }

    public WorkspaceMember resolveWorkspaceMembership(Workspace workspace, AppUser user, String message) {
        return workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new AccessDeniedException(message));
    }

    public void requiredWorkspaceAdminOrAbove(Workspace workspace, AppUser user) {
        WorkspaceMember member = resolveWorkspaceMembership(workspace, user);
        if (Role.MEMBER == member.getRole())
            throw new AccessDeniedException("Only Admin or Owner can perform the action");
    }

    public void requiredWorkspaceOwner(Workspace workspace, AppUser user) {
        WorkspaceMember member = resolveWorkspaceMembership(workspace, user);
        if (Role.OWNER != member.getRole())
            throw new AccessDeniedException("Only Owner can perform the action");
    }

    public void requiredWorkspaceMember(Workspace workspace, AppUser user) {
           resolveWorkspaceMembership(workspace, user);
    }

    public boolean isWorkspaceAdminOrOwner(Workspace workspace, AppUser user) {
        return workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .map(m -> m.getRole() == Role.ADMIN || m.getRole() == Role.OWNER)
                .orElse(false);
    }

    // Standardized to 403 — see resolveWorkspaceMembership above.
    public BoardMember resolveBoardMember(Board board, AppUser user) {
        return boardMemberRepository.findByBoardAndUser(board, user)
                .orElseThrow(() -> new AccessDeniedException("User is not a member"));
    }

    public void requiredBoardMemberOrAbove(Board board, AppUser user) {
        if (isWorkspaceAdminOrOwner(board.getWorkspace(), user)) return;
        BoardMember member = resolveBoardMember(board, user);
        if (member.getRole() == BoardMember.Role.OBSERVER)
            throw new AccessDeniedException("Observers cannot perform this action");
    }

    public void requiredBoardAdmin(Board board, AppUser user) {
        if (isWorkspaceAdminOrOwner(board.getWorkspace(), user)) return;
        BoardMember member = resolveBoardMember(board, user);
        if (member.getRole() != BoardMember.Role.ADMIN)
            throw new AccessDeniedException("Board Admin Action Required");
    }

    public boolean isBoardAdmin(Board board, AppUser user) {
        if (isWorkspaceAdminOrOwner(board.getWorkspace(), user)) return true;
        return boardMemberRepository.findByBoardAndUser(board, user)
                .map(m -> m.getRole() == BoardMember.Role.ADMIN)
                .orElse(false);
    }

    public void checkBoardViewAccess(Board board, AppUser user) {
        if (isWorkspaceAdminOrOwner(board.getWorkspace(), user)) return;
        switch (board.getVisibility()) {
            case PUBLIC -> { }
            case WORKSPACE -> {
                if (!workspaceMemberRepository.existsByWorkspaceAndUser(board.getWorkspace(), user))
                    throw new AccessDeniedException("You are not member of board or Workspace");
            }
            case PRIVATE -> {
                if (!boardMemberRepository.existsByBoardAndUser(board, user))
                    throw new AccessDeniedException("You are not member of this board");
            }
        }
    }

}