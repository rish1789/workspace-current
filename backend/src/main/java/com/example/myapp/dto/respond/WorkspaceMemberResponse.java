package com.example.myapp.dto.respond;

import com.example.myapp.entity.WorkspaceMember.Role;

public class WorkspaceMemberResponse {

    private Integer workspaceId;
    private Integer userId;
    private String  username;
    private Role    role;
    private String  joinedAt;

    public WorkspaceMemberResponse() {}

    public WorkspaceMemberResponse(Integer workspaceId, Integer userId, String username, Role role, String joinedAt) {
        this.workspaceId = workspaceId;
        this.userId      = userId;
        this.username    = username;
        this.role        = role;
        this.joinedAt    = joinedAt;
    }

    public Integer getWorkspaceId() { return workspaceId; }
    public Integer getUserId()      { return userId;      }
    public String  getUsername()    { return username;    }
    public Role    getRole()        { return role;        }
    public String  getJoinedAt()    { return joinedAt;    }

    public void setWorkspaceId(Integer workspaceId) { this.workspaceId = workspaceId; }
    public void setUserId(Integer userId)            { this.userId      = userId;      }
    public void setUsername(String username)         { this.username    = username;    }
    public void setRole(Role role)                   { this.role        = role;        }
    public void setJoinedAt(String joinedAt)         { this.joinedAt    = joinedAt;    }
}