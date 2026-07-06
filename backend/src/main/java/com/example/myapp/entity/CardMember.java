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
import jakarta.persistence.UniqueConstraint;

/**
 * Represents the assignment of a User to a Card.
 *
 * JPA Mapping:
 *  - Maps to the "card_members" table
 *  - card and user are @ManyToOne relationships
 *  - assignedAt is set automatically before insert via @PrePersist
 *  - No setters — immutable join entity
 */
@Entity
@Table(name = "card_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"card_id", "user_id"})
})
public class CardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @JsonIgnore  // prevents infinite recursion when serializing to JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    private static final DateTimeFormatter FORMATTER =
                         DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    protected CardMember() {}

    @PrePersist
    private void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }

    public CardMember(Card card, AppUser user) {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        this.card = card;
        this.user = user;
    }

    public Integer getId()   { return id;   }
    public Card    getCard() { return card; }
    public AppUser    getUser() { return user; }

    public String getAssignedAt() {
        return assignedAt.format(FORMATTER);
    }
}