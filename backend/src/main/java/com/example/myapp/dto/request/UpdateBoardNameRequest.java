package com.example.myapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/boards/{id}/name
 * Contains the new board name.
 */
public class UpdateBoardNameRequest {

    @NotBlank(message = "Board name cannot be null or blank")
    @Size(min=1,max = 100,message = "Board name max character limit is 100")
    private String     boardName;

    public String getBoardName()                  { return boardName;        }
    public void   setBoardName(String boardName)  { this.boardName = boardName; }
}