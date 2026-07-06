package com.example.myapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.myapp.dto.request.AddWorkspaceMemberRequest;
import com.example.myapp.dto.request.CreateWorkspaceRequest;
import com.example.myapp.dto.request.TransferOwnershipRequest;
import com.example.myapp.dto.request.UpdateWorkspaceDescriptionRequest;
import com.example.myapp.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.example.myapp.dto.request.UpdateWorkspaceNameRequest;
import com.example.myapp.dto.respond. WorkspaceMemberResponse;
import com.example.myapp.dto.respond. WorkspaceResponse;
import com.example.myapp.entity.WorkspaceMember.Role;
import com.example.myapp.service.WorkspaceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for Workspace operations.
 *
 * Base URL: /api/workspaces
 *
 * Endpoints:
 *  POST   /api/workspaces                                    — create workspace
 *  GET    /api/workspaces/{id}                               — get workspace by ID
 *  PATCH  /api/workspaces/{id}/name                          — update name
 *  PATCH  /api/workspaces/{id}/description                   — update description
 *  DELETE /api/workspaces/{id}                               — delete workspace
 *  POST   /api/workspaces/{id}/members                       — add member
 *  DELETE /api/workspaces/{id}/members/{userId}              — remove member
 *  GET    /api/workspaces/{id}/members                       — get all members
 *  GET    /api/workspaces/{id}/members/{userId}/role         — get member role
 *  PATCH  /api/workspaces/{id}/members/{userId}/role         — update member role
 */
@RestController
@RequestMapping("/api/workspaces")
@Validated
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    

    // ─── WORKSPACE CRUD ──────────────────────────────────────────────────────

    /**
     * POST /api/workspaces
     * Creates a new workspace.
     * Returns 201 Created with the new workspace.
     */
    @PostMapping
    public ResponseEntity< WorkspaceResponse> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
         WorkspaceResponse respond = workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respond);
    }

    
    @GetMapping("/all")
    public ResponseEntity<List< WorkspaceResponse>> getAllWrkspaceByUserId(){
        return ResponseEntity.ok(workspaceService.getAllWorkspace());
    }
    
    /**
     * GET /api/workspaces/{id}
     * Returns a workspace by its ID.
     */

    @GetMapping("/{id}")
    public ResponseEntity< WorkspaceResponse> getWorkspaceById(@Positive(message = "Invalid Id") @PathVariable Integer id) {
        return ResponseEntity.ok(workspaceService.getWorkspaceById(id));
    }

    /**
     * PATCH /api/workspaces/{id}/name
     * Updates the workspace name.
     */
    @PatchMapping("/{id}/name")
    public ResponseEntity< WorkspaceResponse> updateWorkspaceName(@Positive(message = "Invalid Id") @PathVariable Integer id,
                                                    @Valid @RequestBody UpdateWorkspaceNameRequest request) {
        return ResponseEntity.ok(workspaceService.updateWorkspaceName(id,request));
    }

    /**
     * PATCH /api/workspaces/{id}/description
     * Updates the workspace description.
     */
    @PatchMapping("/{id}/description")
    public ResponseEntity< WorkspaceResponse> updateDescription(@Positive(message = "Invalid Id") @PathVariable Integer id,
                                                  @Valid @RequestBody UpdateWorkspaceDescriptionRequest request) {
        return ResponseEntity.ok(workspaceService.updateDescription(id,request));
    }

    /**
     * DELETE /api/workspaces/{id}
     * Deletes a workspace and all its members.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@Positive(message = "Invalid Id") @PathVariable Integer id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build();
    }

    // ─── MEMBERSHIP MANAGEMENT ───────────────────────────────────────────────

    /**
     * POST /api/workspaces/{id}/members
     * Adds a new member to a workspace.
     */
    @PostMapping("/{id}/members")
    public ResponseEntity< WorkspaceMemberResponse> addMember(@Positive(message = "Invalid Id") @PathVariable Integer id,
                                          @Valid @RequestBody AddWorkspaceMemberRequest request) {
        return ResponseEntity.ok(workspaceService.addMember(id,request));
    }

    /**
     * DELETE /api/workspaces/{id}/members/{userId}
     * Removes a member from a workspace.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                            @Positive(message = "Invalid Id")@PathVariable Integer userId) {
        workspaceService.removeMember(id, userId);
        return ResponseEntity.noContent().build();

    }

    /**
     * GET /api/workspaces/{id}/members
     * Returns all members of a workspace.
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<List< WorkspaceMemberResponse>> getMembers(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(workspaceService.getMembers(id));
    }

    /**
     * GET /api/workspaces/{id}/members/{userId}/role
     * Returns the role of a specific member.
     */
    @GetMapping("/{id}/members/{userId}/role")
    public ResponseEntity<Role> getMemberRole( @Positive(message = "Invalid Id")@PathVariable Integer id,
                                              @Positive(message = "Invalid Id")@PathVariable Integer userId) {
        return ResponseEntity.ok(workspaceService.getMemberRole(id, userId));
    }

    /**
     * PATCH /api/workspaces/{id}/members/{userId}/role
     * Updates the role of a workspace member.
     */
    @PatchMapping("/{id}/members/{userId}/role")
    public ResponseEntity< WorkspaceMemberResponse> updateMemberRole(
    @Positive(message = "Invalid Id") @PathVariable Integer id,
    @Positive(message = "Invalid Id") @PathVariable Integer userId,
    @Valid @RequestBody UpdateWorkspaceMemberRoleRequest request){
        
        return ResponseEntity.ok(workspaceService.updateMemberRole(id, userId,request));
    }

    @PatchMapping("/{id}/members/transfer-ownership")
    public ResponseEntity<Void> setOwnerShip(@Positive(message = "Invalid Id")@PathVariable Integer id,@Valid @RequestBody TransferOwnershipRequest request){
        workspaceService.transferOwnerShip(id, request);
        return ResponseEntity.noContent().build();
    }
}