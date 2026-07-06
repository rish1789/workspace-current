// MoveLaneRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request body for PATCH /api/lanes/{id}/position
 * Contains the new position to move the lane to.
 * Lane ID comes from the URL path variable — not needed in body.
 */
public class MoveLaneRequest {
    
    @PositiveOrZero(message = "Invalid Position")
    private Integer position;

    public Integer getPosition()                  { return position;          }
    public void    setPosition(Integer position)  { this.position = position; }
}