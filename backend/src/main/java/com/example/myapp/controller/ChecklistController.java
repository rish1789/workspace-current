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

import com.example.myapp.dto.request.AddChecklistItemRequest;
import com.example.myapp.dto.request.CreateChecklistRequest;
import com.example.myapp.dto.request.RenameChecklistRequest;
import com.example.myapp.dto.request.UpdateChecklistItemContentRequest;
import com.example.myapp.dto.respond.ChecklistItemResponse;
import com.example.myapp.dto.respond.ChecklistResponse;
import com.example.myapp.service.ChecklistService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for Checklist operations.
 *
 * Base URL: /api/checklists
 *
 * Endpoints:
 *  POST   /api/checklists                       — create checklist
 *  GET    /api/checklists/{id}                  — get checklist by ID
 *  GET    /api/checklists/card/{cardId}         — get checklists by card
 *  PATCH  /api/checklists/{id}/title            — rename checklist
 *  DELETE /api/checklists/{id}                  — delete checklist
 *  POST   /api/checklists/{id}/items            — add item
 *  GET    /api/checklists/items/{itemId}        — get item by ID
 *  GET    /api/checklists/{id}/items            — get items by checklist
 *  PATCH  /api/checklists/items/{id}/content    — update item content
 *  PATCH  /api/checklists/items/{id}/complete   — complete item
 *  PATCH  /api/checklists/items/{id}/uncomplete — uncomplete item
 *  DELETE /api/checklists/items/{id}            — delete item
 */
@RestController
@RequestMapping("/api/checklists")
@Validated
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    // ─── CHECKLIST CRUD ──────────────────────────────────────────────────────

    /**
     * POST /api/checklists
     * Creates a new checklist on a card.
     * Returns 201 Created with the new checklist.
     */
    @PostMapping
    public ResponseEntity<ChecklistResponse> createChecklist(@Valid@RequestBody CreateChecklistRequest request) {
        ChecklistResponse checklist = checklistService.createChecklist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(checklist);
    }

    /**
     * GET /api/checklists/{id}
     * Returns a checklist by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponse> getChecklistById(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(checklistService.getChecklistById(id));
    }

    /**
     * GET /api/checklists/card/{cardId}
     * Returns all checklists on a specific card.
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<ChecklistResponse>> getChecklistsByCardId(@Positive(message = "Invalid Id")@PathVariable Integer cardId) {
        return ResponseEntity.ok(checklistService.getChecklistsByCardId(cardId));
    }

    /**
     * PATCH /api/checklists/{id}/title
     * Renames a checklist.
     */
    @PatchMapping("/{id}/title")
    public ResponseEntity<ChecklistResponse> renameChecklist(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                                @Valid@RequestBody RenameChecklistRequest request) {
        return ResponseEntity.ok(checklistService.renameChecklist(id, request));
    }

    /**
     * DELETE /api/checklists/{id}
     * Deletes a checklist and all its items.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChecklist(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        checklistService.deleteChecklist(id);
        return ResponseEntity.noContent().build();
    }

    // ─── ITEM CRUD ───────────────────────────────────────────────────────────

    /**
     * POST /api/checklists/{id}/items
     * Adds a new item to a checklist.
     * Returns 201 Created with the new item.
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<ChecklistItemResponse> addItem(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                                 @Valid@RequestBody AddChecklistItemRequest request) {
        ChecklistItemResponse item = checklistService.addItem(id,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    /**
     * GET /api/checklists/items/{itemId}
     * Returns a checklist item by its ID.
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ChecklistItemResponse> getItemById(@Positive(message = "Invalid Id")@PathVariable Integer itemId) {
        return ResponseEntity.ok(checklistService.getChecklistItemById(itemId));
    }

    /**
     * GET /api/checklists/{id}/items
     * Returns all items in a specific checklist.
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ChecklistItemResponse>> getItemsByChecklistId(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(checklistService.getChecklistItemsByChecklistId(id));
    }

    /**
     * PATCH /api/checklists/items/{id}/content
     * Updates the content of a checklist item.
     */
    @PatchMapping("/items/{id}/content")
    public ResponseEntity<ChecklistItemResponse> updateContent(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                              @Valid@RequestBody UpdateChecklistItemContentRequest request) {
        return ResponseEntity.ok(checklistService.updateItemContent(id, request));
    }

    /**
     * PATCH /api/checklists/items/{id}/complete
     * Marks a checklist item as completed.
     */
    @PatchMapping("/items/{id}/complete")
    public ResponseEntity<ChecklistItemResponse> completeItem(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(checklistService.completeItem(id));
    }

    /**
     * PATCH /api/checklists/items/{id}/uncomplete
     * Marks a checklist item as not completed.
     */
    @PatchMapping("/items/{id}/uncomplete")
    public ResponseEntity<ChecklistItemResponse> uncompleteItem(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(checklistService.uncompleteItem(id));
    }

    /**
     * DELETE /api/checklists/items/{id}
     * Deletes a checklist item permanently.
     * Returns 204 No Content.
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        checklistService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}