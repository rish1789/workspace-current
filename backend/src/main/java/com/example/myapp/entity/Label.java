package com.example.myapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "labels", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"board_id", "name"})
})
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String color;

    private static final String HEX_COLOR_REGEX = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    // ─── CONSTRUCTORS ─────────────────────────────────────────────────────────

    protected Label() {}

    public Label(Board board, String labelName, String color) {
        if (board == null)
            throw new IllegalArgumentException("Board cannot be null");
        if (labelName == null || labelName.isBlank())
            throw new IllegalArgumentException("Label name cannot be empty");
        if (labelName.length() > 100)
            throw new IllegalArgumentException("Label name exceeds 100 characters");
        if (color == null || color.isBlank())
            throw new IllegalArgumentException("Color cannot be empty");
        if (!color.matches(HEX_COLOR_REGEX))
            throw new IllegalArgumentException("Invalid color format — must be #RGB or #RRGGBB");
        this.board = board;
        this.name  = labelName;
        this.color = color;
    }

    // ─── SETTERS ─────────────────────────────────────────────────────────────

    public void setName(String labelName) {
        if (labelName == null || labelName.isBlank())
            throw new IllegalArgumentException("Label name cannot be empty");
        if (labelName.length() > 100)
            throw new IllegalArgumentException("Label name exceeds 100 characters");
        this.name = labelName;
    }

    public void setColor(String color) {
        if (color == null || color.isBlank())
            throw new IllegalArgumentException("Color cannot be empty");
        if (!color.matches(HEX_COLOR_REGEX))
            throw new IllegalArgumentException("Invalid color format — must be #RGB or #RRGGBB");
        this.color = color;
    }

    // ─── GETTERS ─────────────────────────────────────────────────────────────

    public Integer getId()        { return id;    }
    public Board   getBoard()     { return board; }
    public String  getLabelName() { return name;  }
    public String  getColor()     { return color; }
}