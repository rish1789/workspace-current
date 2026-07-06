// AddCommentRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AddCommentRequest {
    
    @Positive(message = "Invalid card Id")
    private Integer cardId;

    

    @NotBlank(message = "Comment cannot be null or blank")
    @Size(min=1,max=500,message ="Comment exceeds 500 characters")
    private String  content;

    public Integer getCardId()  { return cardId;  }

    public String  getContent() { return content; }

    public void setCardId(Integer cardId)    { this.cardId  = cardId;  }
   
    public void setContent(String content)   { this.content = content; }
}