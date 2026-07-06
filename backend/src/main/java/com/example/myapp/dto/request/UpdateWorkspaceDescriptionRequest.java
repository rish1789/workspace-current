package com.example.myapp.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateWorkspaceDescriptionRequest {

    @Size(max = 500, message = "Description exceeds 500 characters")
    private String description;

    public String getDescription() { return description; }
    public void setDescription(String text) { this.description = text; }
}