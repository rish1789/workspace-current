package com.example.myapp.dto.respond;


public class CommentResponse {
    
    private Integer id;
    private Integer cardId;
    private Integer userId;
    private String content;
    private String createdAt;
    private String updatedAt;

    public CommentResponse(Integer id,Integer cardId,Integer userId,String content,String createdAt,String updatedAt){
        this.id = id;
        this.cardId = cardId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId(){
        return this.id;
    }

    public Integer getCardId(){
        return cardId;
    }
    public Integer getUserId(){return this.userId;}
    public String getContent(){ return this.content;}
    public String getCreatedAt(){ return this.createdAt;}
    public String getUpdatedAt(){ return this.updatedAt;}
}
