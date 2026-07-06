// UpdateWorkspaceNameRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateWorkspaceNameRequest {
    
    @NotBlank(message = "Workspace name cannot be null or blank")
    @Size(min=1,max=100,message = "Workspace name max character limit 100")
    private String workspaceName;

    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
}