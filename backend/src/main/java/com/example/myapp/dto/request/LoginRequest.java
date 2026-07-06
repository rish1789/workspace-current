package com.example.myapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    public String getEmail()    { return email;    }
    public String getPassword() { return password; }

    public void setEmail(String email)       { this.email    = email;    }
    public void setPassword(String password) { this.password = password; }
}