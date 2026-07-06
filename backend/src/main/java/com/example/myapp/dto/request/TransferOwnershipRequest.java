package com.example.myapp.dto.request;

import jakarta.validation.constraints.Positive;

public class TransferOwnershipRequest {
    @Positive(message = "Invalid user Id")
    private Integer userId;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
