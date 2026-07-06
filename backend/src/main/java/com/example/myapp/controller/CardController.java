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

import com.example.myapp.dto.request.AddCardMemberRequest;
import com.example.myapp.dto.request.AttachLabelRequest;
import com.example.myapp.dto.request.CreateCardRequest;
import com.example.myapp.dto.request.MoveCardRequest;
import com.example.myapp.dto.request.UpdateCardDescriptionRequest;
import com.example.myapp.dto.request.UpdateCardDueDateRequest;
import com.example.myapp.dto.request.UpdateCardTitleRequest;
import com.example.myapp.dto.respond.ActivityLogResponse;
import com.example.myapp.dto.respond.CardLabelResponse;
import com.example.myapp.dto.respond.CardMemberResponse;
import com.example.myapp.dto.respond.CardResponse;
import com.example.myapp.service.CardService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for Card operations.
 *
 * Base URL: /api/cards
 *
 * Endpoints:
 *  POST   /api/cards                        — create card
 *  GET    /api/cards/{id}                   — get card by ID
 *  GET    /api/cards/lane/{laneId}          — get cards by lane
 *  PATCH  /api/cards/{id}/title             — update title
 *  PATCH  /api/cards/{id}/description       — update description
 *  PATCH  /api/cards/{id}/due-date          — set due date
 *  PATCH  /api/cards/{id}/move              — move card
 *  PATCH  /api/cards/{id}/archive           — archive card
 *  PATCH  /api/cards/{id}/unarchive         — unarchive card
 *  DELETE /api/cards/{id}                   — delete card
 *  POST   /api/cards/{id}/members           — assign member
 *  DELETE /api/cards/{id}/members/{userId}  — remove member
 *  GET    /api/cards/{id}/members           — get card members
 *  POST   /api/cards/{id}/labels            — attach label
 *  DELETE /api/cards/{id}/labels/{labelId}  — detach label
 *  GET    /api/cards/{id}/labels            — get card labels
 */
@RestController
@RequestMapping("/api/cards")
@Validated
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // ─── CARD CRUD ────────────────────────────────────────────────────────────

    /**
     * POST /api/cards
     * Creates a new card in a lane.
     * Returns 201 Created with the new card.
     */
    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid@RequestBody CreateCardRequest request) {
        CardResponse card = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    /**
     * GET /api/cards/{id}
     * Returns a card by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    /**
     * GET /api/cards/lane/{laneId}
     * Returns all cards in a specific lane.
     */
    @GetMapping("/lane/{laneId}")
    public ResponseEntity<List<CardResponse>> getCardsByLaneId(@Positive(message = "Invalid Id")@PathVariable Integer laneId) {
        return ResponseEntity.ok(cardService.getCardsByLaneId(laneId));
    }

    // ─── CARD UPDATES ────────────────────────────────────────────────────────

    /**
     * PATCH /api/cards/{id}/title
     * Updates the card title.
     */
    @PatchMapping("/{id}/title")
    public ResponseEntity<CardResponse> updateTitle(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                            @Valid@RequestBody UpdateCardTitleRequest request) {
        
        return ResponseEntity.ok(cardService.updateTitle(id, request));
    }

    /**
     * PATCH /api/cards/{id}/description
     * Updates the card description.
     * Null is accepted — clears the description.
     */
    @PatchMapping("/{id}/description")
    public ResponseEntity<CardResponse> updateDescription(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                                  @Valid@RequestBody UpdateCardDescriptionRequest request) {
        
        return ResponseEntity.ok(cardService.updateDescription(id, request));
    }

    /**
     * PATCH /api/cards/{id}/due-date
     * Sets or clears the due date of a card.
     * Null clears the due date.
     */
    @PatchMapping("/{id}/due-date")
    public ResponseEntity<CardResponse> setDueDate(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                           @Valid@RequestBody UpdateCardDueDateRequest request) {
        
        return ResponseEntity.ok(cardService.setDueDate(id, request));
    }

    /**
     * PATCH /api/cards/{id}/move
     * Moves a card to a new position within the same or different lane.
     */
    @PatchMapping("/{id}/move")
    public ResponseEntity<CardResponse> moveCard(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                         @Valid@RequestBody MoveCardRequest request) {
        
        return ResponseEntity.ok(cardService.moveCard(id, request));
    }

    /**
     * PATCH /api/cards/{id}/archive
     * Archives a card — hides it without deleting.
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<CardResponse> archiveCard(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        
        return ResponseEntity.ok(cardService.archiveCard(id));
    }

    /**
     * PATCH /api/cards/{id}/unarchive
     * Unarchives a card — makes it visible again.
     */
    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<CardResponse> unarchiveCard(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        
        return ResponseEntity.ok(cardService.unarchiveCard(id));
    }

    /**
     * DELETE /api/cards/{id}
     * Deletes a card and all its members and labels.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    // ─── MEMBER MANAGEMENT ───────────────────────────────────────────────────

    /**
     * POST /api/cards/{id}/members
     * Assigns a user to a card.
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<CardMemberResponse> assignMember(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                             @Valid@RequestBody AddCardMemberRequest request) {
        
        return ResponseEntity.ok(cardService.assignMember(id, request));
    }

    /**
     * DELETE /api/cards/{id}/members/{userId}
     * Removes a user assignment from a card.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                             @Positive(message = "Invalid Id")@PathVariable Integer userId) {
        cardService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/cards/{id}/members
     * Returns all members assigned to a card.
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<List<CardMemberResponse>> getCardMembers(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(cardService.getCardMembers(id));
    }

    // ─── LABEL MANAGEMENT ────────────────────────────────────────────────────

    /**
     * POST /api/cards/{id}/labels
     * Attaches a label to a card.
     */
    @PostMapping("/{id}/labels")
    public ResponseEntity<CardLabelResponse> attachLabel(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                            @Valid@RequestBody AttachLabelRequest request) {
        
        return ResponseEntity.ok(cardService.attachLabel(id, request));
    }

    /**
     * DELETE /api/cards/{id}/labels/{labelId}
     * Detaches a label from a card.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}/labels/{labelId}")
    public ResponseEntity<Void> detachLabel(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                            @Positive(message = "Invalid Id")@PathVariable Integer labelId) {
        cardService.detachLabel(id, labelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/cards/{id}/labels
     * Returns all labels attached to a card.
     */
    @GetMapping("/{id}/labels")
    public ResponseEntity<List<CardLabelResponse>> getCardLabels(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(cardService.getCardLabels(id));
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<List<ActivityLogResponse>> getActivityLog(
        @Positive(message = "Invalid Id") @PathVariable Integer id) {
        return ResponseEntity.ok(cardService.getActivityLog(id));
    }
}