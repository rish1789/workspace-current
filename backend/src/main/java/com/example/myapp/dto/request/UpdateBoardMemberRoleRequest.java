// UpdateBoardMemberRoleRequest.java
package com.example.myapp.dto.request;

import com.example.myapp.entity.BoardMember.Role;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for PATCH /api/boards/{id}/members/{userId}/role
 * Contains the new role to assign.
 */
public class UpdateBoardMemberRoleRequest {
    
    @NotNull(message = "Invalid parameter")
    private Role role;

    public Role getRole()              { return role;        }
    public void setRole(Role role)     { this.role = role;   }
}