package com.example.myapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents the attachment of a Label to a Card.
 *
 * JPA Mapping:
 *  - Maps to the "card_labels" table
 *  - card and label are @ManyToOne relationships
 *  - No setters — immutable join entity
 */
@Entity
@Table(name = "card_labels", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"card_id", "label_id"})
})
public class CardLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @JsonIgnore  // prevents infinite recursion when serializing to JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;

    protected CardLabel() {}

    public CardLabel(Card card, Label label) {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");
        if (label == null)
            throw new IllegalArgumentException("Label cannot be null");
        this.card  = card;
        this.label = label;
    }

    public Integer getId()    { return id;    }
    public Card    getCard()  { return card;  }
    public Label   getLabel() { return label; }
}