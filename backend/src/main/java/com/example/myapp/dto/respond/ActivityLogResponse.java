package com.example.myapp.dto.respond;

public class ActivityLogResponse {
    private Integer id;
    private Integer cardId;
    private Integer userId;
    private String  action;
    private String  createdAt;

    public ActivityLogResponse(Integer id, Integer cardId, Integer userId,
                                String action, String createdAt) {
        this.id        = id;
        this.cardId    = cardId;
        this.userId    = userId;
        this.action    = action;
        this.createdAt = createdAt;
    }

    public Integer getId()        { return id;        }
    public Integer getCardId()    { return cardId;    }
    public Integer getUserId()    { return userId;    }
    public String  getAction()    { return action;    }
    public String  getCreatedAt() { return createdAt; }
}