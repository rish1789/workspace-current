// RenameLaneRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/lanes/{id}/name
 * Contains the new lane name.
 */
public class RenameLaneRequest {
    
    @NotBlank(message ="Invalid Name : Lane name cannot be null or blank")
    @Size(min=1,max=100,message="Lane name exceeds 100 characters")
    private String laneName;

    public String getLaneName()                 { return laneName;        }
    public void   setLaneName(String laneName)  { this.laneName = laneName; }
}