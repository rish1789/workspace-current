package com.example.myapp.dto.request;

import com.example.myapp.entity.Board.Visibility;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for PATCH /api/boards/{id}/visibility
 * Contains the new visibility scope.
 * Must be one of: PRIVATE, WORKSPACE, PUBLIC
 */
public class UpdateBoardVisibilityRequest {
    
    @NotNull(message = "Invalid parameter")
    private Visibility visibility;

    public Visibility getVisibility()                        { return visibility;              }
    public void       setVisibility(Visibility visibility)   { this.visibility = visibility;   }
}