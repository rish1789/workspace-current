package com.example.myapp.dto.respond;

public class CardMemberResponse {
    private Integer id;
    private Integer userId;
    private Integer cardId;
    private String  username;
    private String  assignedAt;


    public CardMemberResponse(Integer id,Integer userId, Integer cardId,String username,String assignedAt){

        this.id = id;
        this.userId = userId;
        this.cardId = cardId;
        this.username = username;
        this.assignedAt = assignedAt;
    }

    public Integer getId(){
        return id;
    }

    public Integer getUserId(){
        return userId;
    }

    public Integer getCardId(){
        return cardId;
    }
    public String getUsername(){
        return username;
    }
    public String getAssignedAt(){
        return assignedAt;
    }


}
