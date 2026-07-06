package com.example.myapp.dto.respond;



public class LabelResponse {
    private Integer labelId;
    private Integer boardId;
    private String  labelName;
    private String  color; 
   
    public LabelResponse(){}
    public LabelResponse(Integer labelId,Integer boardId,String labelName,String color){
        this.labelId = labelId;
        this.boardId = boardId;
        this.labelName = labelName;
        this.color    = color; 
    }
    
    public void setLabelId(Integer labelId)    {this.labelId = labelId;}
    public void setBoardId(Integer boardId)    {this.boardId = boardId;}
    public void setLabelName(String labelName) {this.labelName = labelName;}
    public void setColor(String color)         {this.color = color;}
    
    public Integer getLabelId()        { return labelId;}
    public Integer getBoard()     { return boardId; }
    public String  getLabelName() { return labelName;  }
    public String  getColor()     { return color; }
}
