// AddBoardMemberRequest.java
package com.example.myapp.dto.request;

import com.example.myapp.entity.BoardMember.Role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for POST /api/boards/{id}/members
 * Contains the user ID and role to assign.
 */
public class AddBoardMemberRequest {
    @Positive(message = "Invalid workspace id")
    private Integer userId;

    @NotNull(message = "Invalid parameter")
    private Role    role;

    public Integer getUserId() { return userId; }
    public Role    getRole()   { return role;   }

    public void setUserId(Integer userId) { this.userId = userId; }
    public void setRole(Role role)        { this.role   = role;   }
}