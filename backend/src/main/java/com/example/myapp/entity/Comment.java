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
 * Represents a comment left by a user on a Card.
 *
 * JPA Mapping:
 *  - Maps to the "comments" table
 *  - card and user are @ManyToOne relationships
 *  - createdAt and updatedAt are set automatically via @PrePersist
 *  - updatedAt is refreshed every time setContent() is called
 */
@Entity
@Table(name = "comments")
public class Comment {

    // ─── FIELDS ──────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @JsonIgnore  // prevents infinite recursion when serializing to JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false, updatable = false)
    private Card card;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private AppUser user;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private static final DateTimeFormatter FORMATTER =
                         DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── JPA REQUIRED NO-ARG CONSTRUCTOR ─────────────────────────────────────

    protected Comment() {}

    // ─── JPA LIFECYCLE CALLBACK ───────────────────────────────────────────────

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public Comment(Card card, AppUser user, String content) {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Content cannot be null or blank");
        if (content.length() > 500)
            throw new IllegalArgumentException("Content exceeds 500 characters");

        this.card    = card;
        this.user    = user;
        this.content = content;
    }

    // ─── SETTERS ─────────────────────────────────────────────────────────────

    /**
     * Updates the comment content and refreshes updatedAt timestamp.
     */
    public void setContent(String text) {
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("Content cannot be null or blank");
        if (text.length() > 500)
            throw new IllegalArgumentException("Content exceeds 500 characters");
        this.content   = text;
        this.updatedAt = LocalDateTime.now();  // refresh on every edit
    }

    // ─── GETTERS ─────────────────────────────────────────────────────────────

    public Integer getId()      { return id;      }
    public Card    getCard()    { return card;    }
    public AppUser getUser()    { return user;    }
    public String  getContent() { return content; }

    public String getCreatedAt() { return createdAt.format(FORMATTER); }
    public String getUpdatedAt() { return updatedAt.format(FORMATTER); }
}