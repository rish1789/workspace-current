package com.example.myapp.dto.respond;

public class AppUserPublicResponse {
    private Integer id;
    private String  username;

    public AppUserPublicResponse(Integer id, String username) {
        this.id       = id;
        this.username = username;
    }

    public Integer getId()        { return id;       }
    public String  getUsername()  { return username; }
}