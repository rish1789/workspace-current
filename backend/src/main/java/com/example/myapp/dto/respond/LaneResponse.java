package com.example.myapp.dto.respond;

public class LaneResponse {

    private Integer  id;
    private Integer  boardId;
    private String   name;
    private Integer  position;  
    private String   createdAt;
    private boolean  isArchived;

    public LaneResponse(){}
   
    public LaneResponse(Integer id,Integer boardId, String name, Integer position, String createdAt,boolean isArchived){
        this.id = id;
        this.boardId      = boardId;
        this.name       = name;
        this.position   = position;
        this.createdAt  = createdAt;
        this.isArchived = isArchived;
    }

    public Integer getId() {return this.id;}
    public Integer getBoardId(){return this.boardId;}
    public String getName(){return this.name;}
    public Integer getPosition(){return this.position;}
    public String getCreatedAt(){return this.createdAt;}
    public boolean isArchived() { return this.isArchived; }

    public void setId(Integer id){ this.id = id;}
    public void setBoardId(Integer boardId){this.boardId = boardId;}
    public void setName(String name){this.name = name;}
    public void setPosition(Integer position){this.position = position;}
    public void setCreatedAt(String createdAt){this.createdAt = createdAt;}
    public void setArchived(boolean archived){this.isArchived = archived;}
}
