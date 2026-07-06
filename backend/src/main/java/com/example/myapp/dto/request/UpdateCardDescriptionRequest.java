// UpdateCardDescriptionRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/cards/{id}/description
 * Null is accepted — clears the description.
 */
public class UpdateCardDescriptionRequest {

    @Size(max = 500, message = "Description exceeds 500 characters")
    private String description;

    public String getDescription()                     { return description;              }
    public void   setDescription(String description)   { this.description = description;  }
}