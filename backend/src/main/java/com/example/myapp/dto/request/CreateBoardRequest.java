package com.example.myapp.dto.request;

import com.example.myapp.entity.Board.Visibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/boards
 */
public class CreateBoardRequest {
    
    @Positive(message = "Invalid workspace id")
    private Integer    workspaceId;

    @NotBlank(message = "Board name cannot be null or blank")
    @Size(min=1,max = 100,message = "Board name max character limit is 100")
    private String     boardName;

    @NotNull(message = "Invalid parameter")
    private Visibility visibility;

    public Integer    getWorkspaceId()  { return workspaceId; }
    public String     getBoardName()    { return boardName;   }
    public Visibility getVisibility()   { return visibility;  }

    public void setWorkspaceId(Integer workspaceId)      { this.workspaceId = workspaceId; }
    public void setBoardName(String boardName)            { this.boardName   = boardName;   }
   
    public void setVisibility(Visibility visibility)     { this.visibility  = visibility;  }
}