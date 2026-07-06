// MoveCardRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request body for PATCH /api/cards/{id}/move
 * Contains target lane ID and new position.
 */
public class MoveCardRequest {
    @Positive(message = "Invalid Id")
    private Integer laneId;
    @PositiveOrZero(message = "Invalid Position")
    private Integer position;

    public Integer getLaneId()    { return laneId;    }
    public Integer getPosition()  { return position;  }

    public void setLaneId(Integer laneId)      { this.laneId    = laneId;    }
    public void setPosition(Integer position)  { this.position  = position;  }
}