// AddChecklistItemRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class AddChecklistItemRequest {
    @NotBlank(message = "Content cannot be empty")
    @Size(min=1,max = 200,message = "Content exceeds limit 200 characters")
    private String  content;

    @PositiveOrZero(message = "Invalid Position")
    private Integer position;

    public String  getContent()  { return content;  }
    public Integer getPosition() { return position; }

    public void setContent(String content)     { this.content  = content;  }
    public void setPosition(Integer position)  { this.position = position; }
}