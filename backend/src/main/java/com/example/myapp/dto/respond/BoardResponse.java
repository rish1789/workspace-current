package com.example.myapp.dto.respond;

import com.example.myapp.entity.Board.Visibility;

public class BoardResponse {

    private Integer    boardId;
    private String     boardName;
    private Integer    workspaceId;
    private Integer    createdBy;
    private Visibility visibility;
    private String     createdAt;

    public BoardResponse() {}

    public BoardResponse(Integer boardId, String boardName, Integer workspaceId,
                        Integer createdBy, Visibility visibility, String createdAt) {
        this.boardId      = boardId;
        this.boardName    = boardName;
        this.workspaceId  = workspaceId;
        this.createdBy    = createdBy;
        this.visibility   = visibility;
        this.createdAt    = createdAt;
    }

    public Integer    getBoardId()     { return boardId;     }
    public String     getBoardName()   { return boardName;   }
    public Integer    getWorkspaceId() { return workspaceId; }
    public Integer    getCreatedBy()   { return createdBy;   }
    public Visibility getVisibility()  { return visibility;  }
    public String     getCreatedAt()   { return createdAt;   }

    public void setBoardId(Integer boardId)          { this.boardId     = boardId;     }
    public void setBoardName(String boardName)        { this.boardName   = boardName;   }
    public void setWorkspaceId(Integer workspaceId)  { this.workspaceId = workspaceId; }
    public void setCreatedBy(Integer createdBy)      { this.createdBy   = createdBy;   }
    public void setVisibility(Visibility visibility) { this.visibility  = visibility;  }
    public void setCreatedAt(String createdAt)       { this.createdAt   = createdAt;   }
}