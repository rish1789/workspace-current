package com.example.myapp.dto.respond;

public class ChecklistItemResponse {
   private Integer id;
   private Integer checklistId;
   private String  content;
   private boolean isDone;
   private Integer position;

   public ChecklistItemResponse(Integer id,Integer chklstId,String content,boolean isDone ,Integer position){
     this.id=id;
     this.checklistId = chklstId;
     this.content = content;
     this.isDone = isDone;
     this.position = position;
   }

   public Integer getId(){
    return id;
   }

   public Integer getcheckListId(){
    return this.checklistId;
   }
   public String getContent(){
    return this.content;
   }

   public boolean isDone(){
    return this.isDone;
   }

   public Integer getPosition(){
    return this.position;
   }
}
