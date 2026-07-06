// AddCardMemberRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.Positive;

/**
 * Request body for POST /api/cards/{id}/members
 * Contains the user ID to assign.
 */
public class AddCardMemberRequest {
    @Positive(message = "Invalid Id")
    private Integer userId;

    public Integer getUserId()                { return userId;         }
    public void    setUserId(Integer userId)  { this.userId = userId;  }
}