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
import com.example.myapp.entity.BoardMember.Role;
import com.example.myapp.service.BoardService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/boards")
@Validated
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // ─── BOARD CRUD ──────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoardById(@Positive(message = "Invalid ID") @PathVariable Integer id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<BoardResponse>> getBoardsByWorkspaceId(@Positive(message = "Invalid ID") @PathVariable Integer workspaceId) {
        return ResponseEntity.ok(boardService.getBoardsByWorkspaceId(workspaceId));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<BoardResponse> updateBoardName(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                                   @Valid @RequestBody UpdateBoardNameRequest request) {
        return ResponseEntity.ok(boardService.updateBoardName(id, request));
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<BoardResponse> updateDescription(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                                     @Valid @RequestBody UpdateBoardDescriptionRequest request) {
        return ResponseEntity.ok(boardService.updateDescription(id, request));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<BoardResponse> updateVisibility(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                                    @Valid @RequestBody UpdateBoardVisibilityRequest request) {
        return ResponseEntity.ok(boardService.updateVisibility(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@Positive(message = "Invalid ID") @PathVariable Integer id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    // ─── MEMBERSHIP MANAGEMENT ───────────────────────────────────────────────

    @PostMapping("/{id}/members")
    public ResponseEntity<BoardMemberResponse> addMember(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                             @Valid @RequestBody AddBoardMemberRequest request) {
        return ResponseEntity.ok(boardService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                             @Positive(message = "Invalid ID") @PathVariable Integer userId) {
        boardService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<BoardMemberResponse>> getMembersByBoardId(@Positive(message = "Invalid ID") @PathVariable Integer id) {
        return ResponseEntity.ok(boardService.getMembersByBoardId(id));
    }

    @GetMapping("/{id}/members/{userId}/role")
    public ResponseEntity<Role> getMemberRole(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                              @Positive(message = "Invalid ID") @PathVariable Integer userId) {
        return ResponseEntity.ok(boardService.getMemberRole(id, userId));
    }

    @PatchMapping("/{id}/members/{userId}/role")
    public ResponseEntity<BoardMemberResponse> updateMemberRole(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                                    @Positive(message = "Invalid ID") @PathVariable Integer userId,
                                                    @Valid @RequestBody UpdateBoardMemberRoleRequest request) {
        return ResponseEntity.ok(boardService.updateMemberRole(id, userId, request));
    }

    // ─── LABEL MANAGEMENT ────────────────────────────────────────────────────

    @PostMapping("/{id}/labels")
    public ResponseEntity<LabelResponse> createLabel(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                                     @Valid @RequestBody CreateLabelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createLabel(id, request));
    }

    @GetMapping("/{id}/labels/{labelId}")
    public ResponseEntity<LabelResponse> getLabelById(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                                      @Positive(message = "Invalid ID") @PathVariable Integer labelId) {
        return ResponseEntity.ok(boardService.getLabelById(labelId));
    }

    @GetMapping("/{id}/labels")
    public ResponseEntity<List<LabelResponse>> getLabelsByBoardId(@Positive(message = "Invalid ID") @PathVariable Integer id) {
        return ResponseEntity.ok(boardService.getLabelsByBoardId(id));
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    public ResponseEntity<Void> deleteLabel(@Positive(message = "Invalid ID") @PathVariable Integer id,
                                            @Positive(message = "Invalid ID") @PathVariable Integer labelId) {
        boardService.deleteLabel(labelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/change-admin")
    public ResponseEntity<Void> changeAdmin(
        @Positive(message = "Invalid ID") @PathVariable Integer id,
        @Valid @RequestBody ChangeBoardAdmin request) {
        boardService.changeAdmin(id, request);
        return ResponseEntity.noContent().build();
    }
}