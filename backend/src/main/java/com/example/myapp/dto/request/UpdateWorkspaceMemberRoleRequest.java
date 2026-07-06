// UpdateMemberRoleRequest.java
package com.example.myapp.dto.request;

import com.example.myapp.entity.WorkspaceMember.Role;

import jakarta.validation.constraints.NotNull;

public class UpdateWorkspaceMemberRoleRequest {
    @NotNull(message = "Invalid role")
    private Role role;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}