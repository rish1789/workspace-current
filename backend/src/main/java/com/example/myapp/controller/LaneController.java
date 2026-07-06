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

import com.example.myapp.dto.request.CreateLaneRequest;
import com.example.myapp.dto.request.MoveLaneRequest;
import com.example.myapp.dto.request.RenameLaneRequest;
import com.example.myapp.dto.respond.LaneResponse;
import com.example.myapp.service.LaneService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for Lane operations.
 *
 * Base URL: /api/lanes
 *
 * Endpoints:
 *  POST   /api/lanes                    — create lane
 *  GET    /api/lanes/{id}               — get lane by ID
 *  GET    /api/lanes/board/{boardId}    — get lanes by board
 *  PATCH  /api/lanes/{id}/name          — rename lane
 *  PATCH  /api/lanes/{id}/position      — move lane
 *  PATCH  /api/lanes/{id}/archive       — archive lane
 *  PATCH  /api/lanes/{id}/unarchive     — unarchive lane
 *  DELETE /api/lanes/{id}               — delete lane
 */
@RestController
@RequestMapping("/api/lanes")
@Validated
public class LaneController {

    private final LaneService laneService;

    public LaneController(LaneService laneService) {
        this.laneService = laneService;
    }

    // ─── LANE CRUD ────────────────────────────────────────────────────────────

    /**
     * POST /api/lanes
     * Creates a new lane on a board.
     * Returns 201 Created with the new lane.
     */
    @PostMapping
    public ResponseEntity<LaneResponse> createLane(@Valid@RequestBody CreateLaneRequest request) {
        LaneResponse lane = laneService.createLane(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lane);
    }

    /**
     * GET /api/lanes/{id}
     * Returns a lane by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LaneResponse> getLaneById(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(laneService.getLaneById(id));
    }

    /**
     * GET /api/lanes/board/{boardId}
     * Returns all lanes on a specific board.
     */
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<LaneResponse>> getLanesByBoardId(@Positive(message = "Invalid Id")@PathVariable Integer boardId) {
        return ResponseEntity.ok(laneService.getLanesByBoardId(boardId));
    }

    // ─── LANE UPDATES ────────────────────────────────────────────────────────

    /**
     * PATCH /api/lanes/{id}/name
     * Renames a lane.
     */
    @PatchMapping("/{id}/name")
    public ResponseEntity<LaneResponse> renameLane(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                           @Valid@RequestBody RenameLaneRequest request) {
        return ResponseEntity.ok(laneService.renameLane(id, request));
        
    }

    /**
     * PATCH /api/lanes/{id}/position
     * Moves a lane to a new position.
     * Shifts all affected lanes automatically.
     */
    @PatchMapping("/{id}/position")
    public ResponseEntity<LaneResponse> moveLane(@Positive(message = "Invalid Id")@PathVariable Integer id,
                                         @Valid@RequestBody MoveLaneRequest request) {
        
        return ResponseEntity.ok(laneService.moveLane(id,request));
    }

    /**
     * PATCH /api/lanes/{id}/archive
     * Archives a lane — hides it without deleting.
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<LaneResponse> archiveLane(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(laneService.archiveLane(id));
    }

    /**
     * PATCH /api/lanes/{id}/unarchive
     * Unarchives a lane — makes it visible again.
     */
    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<LaneResponse> unarchiveLane(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        return ResponseEntity.ok(laneService.unarchiveLane(id));
    }

    // ─── LANE DELETE ─────────────────────────────────────────────────────────

    /**
     * DELETE /api/lanes/{id}
     * Deletes a lane permanently.
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLane(@Positive(message = "Invalid Id")@PathVariable Integer id) {
        laneService.deleteLane(id);
        return ResponseEntity.noContent().build();
    }
}