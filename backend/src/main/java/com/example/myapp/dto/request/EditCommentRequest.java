// EditCommentRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditCommentRequest {
    
    
    @NotBlank(message = "Comment cannot be null or blank")
    @Size(min=1,max=500,message ="Comment exceeds 500 characters")
    private String content;

    public String getContent()               { return content;          }
    public void   setContent(String content) { this.content = content;  }
}