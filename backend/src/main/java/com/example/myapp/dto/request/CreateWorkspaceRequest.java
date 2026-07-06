// CreateWorkspaceRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;

public class CreateWorkspaceRequest {
    @NotBlank(message = "Workspace name cannot be blank")
    @Size(min=1,max = 100,message = "Workspace Name max limit is 100 characters" )
    private String  workspaceName;

    

    public String  getWorkspaceName() { return workspaceName; }

    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
  
}