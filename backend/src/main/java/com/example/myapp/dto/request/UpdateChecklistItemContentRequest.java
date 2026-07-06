// UpdateChecklistItemContentRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateChecklistItemContentRequest {
    @NotBlank(message = "Content cannot be empty")
    @Size(min=1,max = 200,message = "Content exceeds limit 200 characters")
    private String content;

    public String getContent()               { return content;          }
    public void   setContent(String content) { this.content = content;  }
}