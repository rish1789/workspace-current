package com.example.myapp.entity;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Represents a lane (column) on a Kanban board.
 * e.g. "To Do", "In Progress", "Done"
 *
 * Responsibilities:
 *  - Stores lane identity (id, name, position)
 *  - Links to its parent board via boardId
 *  - Tracks whether the lane is archived
 *  - Validates all inputs on construction and update
 *
 * Note:
 *  - Does not hold Card objects directly.
 *    Cards belonging to this lane are managed by the service layer.
 *  - Position is zero-based — position 0 means the first lane (leftmost).
 *  - Archiving hides the lane without deleting it.
 */
@Entity
@Table(name = "lanes")
public class Lane {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer       id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, updatable = false)
    private Board         board;

    @Column(nullable = false,length = 100)
    private String        name;
    @Column(nullable = false)
    private Integer       position;
    @Column(name = "is_archived",nullable = false)   // zero-based order within the board (left to right)
    private boolean       isArchived; // default false — lane is visible by default
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    // Shared formatter — defined once at class level to avoid recreating on every call
    private static final DateTimeFormatter FORMATTER =
                         DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    /**
     * Creates a new Lane with full validation.
     *
     * @param id        unique numeric ID (must be > 0)
     * @param boardId   ID of the parent board (must be > 0)
     * @param laneName  display name of the lane (cannot be null or blank)
     * @param position  zero-based position within the board (must be >= 0)
     */
    protected Lane(){}
    @PrePersist
    private void prePersist(){
        this.isArchived = false;                // newly created lanes are visible by default
        this.createdAt  = LocalDateTime.now();  // captured at creation time
    }
    public Lane(Board board, String laneName, Integer position) {
        // Board ID validation
        if (board == null)
            throw new IllegalArgumentException("Board cannot be null");
        // Lane name validation
        if (laneName == null || laneName.isBlank())
            throw new IllegalArgumentException("Invalid Name : Lane name cannot be null or blank");
        if  (laneName.length()>100)
            throw new IllegalArgumentException("Lane name exceeds 100 characters");
        // Position validation — zero-based, so 0 is valid (first lane)
        if (position == null || position < 0)
            throw new IllegalArgumentException("Invalid Position : Position cannot be null or negative");

        this.board      = board;
        this.name       = laneName;
        this.position   = position;
    }

    // ─── SETTERS ─────────────────────────────────────────────────────────────

    /**
     * Updates the lane name.
     * Applies the same validation rules as the constructor.
     */
    public void setName(String laneName) {
        if (laneName == null || laneName.isBlank())
            throw new IllegalArgumentException("Invalid Name : Lane name cannot be null or blank");
        if (laneName.length()>100)
            throw new IllegalArgumentException("Label name exceed character limit Max 100 character");
        this.name = laneName;
    }

    /**
     * Updates the position of this lane within the board.
     * Position is zero-based — 0 means the first (leftmost) lane.
     */
    public void setPosition(Integer position) {
        if (position == null || position < 0)
            throw new IllegalArgumentException("Invalid Position : Position cannot be null or negative");
        this.position = position;
    }

    /**
     * Archives or unarchives the lane.
     * true  = archived (hidden from the board view)
     * false = active   (visible on the board)
     */
    public void archived() {
        this.isArchived = true;
    }

    public void unarchived(){
        this.isArchived = false;
    }



    // ─── GETTERS ─────────────────────────────────────────────────────────────

    public Integer getId()       { return id;       }
    public Board   getBoard()    { return board;  }
    public String  getName()     { return name;     }
    public Integer getPosition() { return position; }

    /**
     * Returns true if this lane is archived, false if it is active.
     * Archived lanes are hidden but not deleted.
     */
    public boolean getArchived() {
        return isArchived;
    }

    /**
     * Returns the lane creation timestamp as a formatted string.
     * Format: dd-MM-yyyy HH:mm:ss
     * createdAt is always set in the constructor so this will never throw.
     */
    public String getCreatedAt() {
        return createdAt.format(FORMATTER);
    }
}