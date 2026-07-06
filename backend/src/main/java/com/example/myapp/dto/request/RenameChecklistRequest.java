// RenameChecklistRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RenameChecklistRequest {
    @NotBlank(message = "Title cannot be empty")
    @Size(min = 1,max = 500,message = "Title exceeds 100 characters")
    private String title;

    public String getTitle()               { return title;        }
    public void   setTitle(String title)   { this.title = title;  }
}