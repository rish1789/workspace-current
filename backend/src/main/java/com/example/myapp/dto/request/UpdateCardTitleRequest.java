// UpdateCardTitleRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/cards/{id}/title
 */
public class UpdateCardTitleRequest {
    @NotBlank(message = "Title cannot be empty")
    @Size(min=1,max = 150,message = "Title exceeds 150 characters")
    private String title;

    public String getTitle()               { return title;        }
    public void   setTitle(String title)   { this.title = title;  }
}