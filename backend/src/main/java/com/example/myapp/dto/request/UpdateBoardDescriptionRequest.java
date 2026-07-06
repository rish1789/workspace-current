package com.example.myapp.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/boards/{id}/description
 * Contains the new board description.
 * Null is accepted — clears the description.
 */
public class UpdateBoardDescriptionRequest {
    @Size(max = 500, message = "Description exceeds 500 characters")
    private String description;

    public String getDescription()                      { return description;             }
    public void   setDescription(String description)   { this.description = description; }
}