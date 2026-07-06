package com.example.myapp.dto.respond;

public class AppUserResponse {

    private Integer id;
    private String username;
    private String email;

    public AppUserResponse() {}

    public AppUserResponse(Integer id, String username,String email) {
        this.id       = id;
        this.username = username;
        this.email    = email;
    }

    public Integer getId()       { return id;       }
    public String  getUsername() { return username; }
    public String  getEmail() { return email; }

    public void setId(Integer id)             { this.id = id;             }
    public void setUsername(String username)  { this.username = username; }
    public void setEmail(String email)        {this.email = email;}
}