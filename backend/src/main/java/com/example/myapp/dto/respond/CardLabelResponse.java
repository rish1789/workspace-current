package com.example.myapp.dto.respond;

public class CardLabelResponse {
    private Integer id;
    private Integer cardId;
    private Integer labelId;
    private String  labelName;
    private String  color;

    public CardLabelResponse(Integer id, Integer cardId, Integer labelId, 
                             String labelName, String color) {
        this.id        = id;
        this.cardId    = cardId;
        this.labelId   = labelId;
        this.labelName = labelName;
        this.color     = color;
    }

    public Integer getId()        { return id;        }
    public Integer getCardId()    { return cardId;    }
    public Integer getLabelId()   { return labelId;   }
    public String  getLabelName() { return labelName; }
    public String  getColor()     { return color;     }
}