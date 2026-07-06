// AttachLabelRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.Positive;

/**
 * Request body for POST /api/cards/{id}/labels
 * Contains the label ID to attach.
 */
public class AttachLabelRequest {
    
    @Positive(message = "Invalid Label Id")
    private Integer labelId;

    public Integer getLabelId()                 { return labelId;          }
    public void    setLabelId(Integer labelId)  { this.labelId = labelId;  }
}