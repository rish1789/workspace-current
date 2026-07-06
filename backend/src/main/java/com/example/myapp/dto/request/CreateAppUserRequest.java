package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class CreateAppUserRequest {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 1,max = 50, message = "Username exceeds 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(
    regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
    message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    @Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,20}$",
    message = "Password must be 8-20 chars with uppercase, lowercase, digit and special character")
    private String password;

    public String getUsername() { return username; }
    public String getEmail()    { return email;    }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email)       { this.email = email;       }
    public void setPassword(String password) { this.password = password; }
}