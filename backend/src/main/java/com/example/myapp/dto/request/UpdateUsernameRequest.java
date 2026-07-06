// UpdateUsernameRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUsernameRequest {
    
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 1,max = 50, message = "Username exceeds 50 characters")
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}