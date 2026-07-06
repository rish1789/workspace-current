// CreateCardRequest.java
package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/cards
 */
public class CreateCardRequest {
    @Positive(message = "Invalid Id")
    private Integer laneId;
    @NotBlank(message = "Title cannot be empty")
    @Size(min=1,max = 150,message = "Title exceeds 150 characters")
    private String  title;
    @PositiveOrZero(message = "Invalid Position")
    private Integer position;


    public Integer getLaneId()    { return laneId;     }
    public String  getTitle()     { return title;      }
    public Integer getPosition()  { return position;   }


    public void setLaneId(Integer laneId)      { this.laneId     = laneId;    }
    public void setTitle(String title)         { this.title      = title;     }
    public void setPosition(Integer position)  { this.position   = position;  }

}