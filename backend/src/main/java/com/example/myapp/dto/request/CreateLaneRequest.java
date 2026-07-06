// CreateLaneRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/lanes
 * Contains all fields required to create a new lane.
 */
public class CreateLaneRequest {
    @Positive(message = "Invalid Id")
    private Integer boardId;

    @NotBlank(message ="Invalid Name : Lane name cannot be null or blank")
    @Size(min=1,max=100,message="Lane name exceeds 100 characters")
    private String  laneName;

    @PositiveOrZero(message = "Invalid Position")
    private Integer position;

    public Integer getBoardId()  { return boardId;  }
    public String  getLaneName() { return laneName; }
    public Integer getPosition() { return position; }

    public void setBoardId(Integer boardId)   { this.boardId  = boardId;  }
    public void setLaneName(String laneName)  { this.laneName = laneName; }
    public void setPosition(Integer position) { this.position = position; }
}