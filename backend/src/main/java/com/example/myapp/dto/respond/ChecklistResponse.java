package com.example.myapp.dto.respond;

public class ChecklistResponse {
   private Integer id;
   private Integer cardId;
   private String  title;

   public ChecklistResponse(Integer id,Integer cardId,String title){
    this.id=id;
    this.cardId=cardId;
    this.title=title;
   }

   public Integer getId(){
    return this.id;
   }

   public Integer getCardId(){
      return this.cardId;
   }

   public String getTitle(){
    return this.title;
   }
}
