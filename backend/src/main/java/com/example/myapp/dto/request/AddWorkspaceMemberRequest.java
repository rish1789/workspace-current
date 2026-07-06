// AddWorkspaceMemberRequest.java
package com.example.myapp.dto.request;

import com.example.myapp.entity.WorkspaceMember.Role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddWorkspaceMemberRequest {
    @Positive(message = "Invalid User Id")
    private Integer userId;
    @NotNull(message = "Invalid member role")
    private Role    role;

    public Integer getUserId() { return userId; }
    public Role    getRole()   { return role;   }

    public void setUserId(Integer userId) { this.userId = userId; }
    public void setRole(Role role)        { this.role   = role;   }
}