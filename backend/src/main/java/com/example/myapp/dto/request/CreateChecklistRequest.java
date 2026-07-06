// CreateChecklistRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateChecklistRequest {
    @Positive(message = "Invalid Card Id")
    private Integer cardId;
    @NotBlank(message = "Title cannot be empty")
    @Size(min = 1,max = 500,message = "Title exceeds 100 characters")
    private String  title;

    public Integer getCardId() { return cardId; }
    public String  getTitle()  { return title;  }

    public void setCardId(Integer cardId) { this.cardId = cardId; }
    public void setTitle(String title)    { this.title  = title;  }
}