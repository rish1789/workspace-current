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

import com.example.myapp.dto.request.AddCommentRequest;
import com.example.myapp.dto.request.EditCommentRequest;
import com.example.myapp.dto.respond.CommentResponse;
import com.example.myapp.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for Comment operations.
 *
 * Base URL: /api/comments
 *
 * Endpoints:
 *  POST   /api/comments                — add comment
 *  GET    /api/comments/{id}           — get comment by ID
 *  GET    /api/comments/card/{cardId}  — get comments by card
 *  PATCH  /api/comments/{id}/content   — edit comment
 *  DELETE /api/comments/{id}           — delete comment
 */
@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ─── COMMENT CRUD ────────────────────────────────────────────────────────

    /**
     * POST /api/comments
     * Adds a new comment to a card.
     * Returns 201 Created with the new comment.
     */
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@Valid@RequestBody AddCommentRequest request) {
        CommentResponse comment = commentService.addComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * GET /api/comments/{id}
     * Returns a comment by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    /**
     * GET /api/comments/card/{cardId}
     * Returns all comments on a specific card.
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByCardId(@Positive(message = "Invalid Id")@PathVariable Integer cardId) {
        return ResponseEntity.ok(commentService.getCommentsByCardId(cardId));
    }

    /**
     * PATCH /api/comments/{id}/content
     * Edits the content of a comment.
     * updatedAt is refreshed automatically.
     */
    @PatchMapping("/{id}/content")
    public ResponseEntity<CommentResponse> editComment(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                            @Valid@RequestBody EditCommentRequest request) {
        
        return ResponseEntity.ok(commentService.editComment(id, request));
    }

    /**
     * DELETE /api/comments/{id}
     * Deletes a comment permanently.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}