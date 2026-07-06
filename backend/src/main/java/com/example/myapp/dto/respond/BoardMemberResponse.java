package com.example.myapp.dto.respond;

import com.example.myapp.entity.BoardMember.Role;

public class BoardMemberResponse {
    private Integer userId;
    private Integer boardId;
    private String  username;
    private Role    role;
    private String  joinedAt;

    public BoardMemberResponse(){}
    public BoardMemberResponse(Integer userId,Integer boardId,String username,Role role,String joinedAt){
        this.userId   = userId;
        this.boardId  = boardId;
        this.username = username;
        this.role     = role;
        this.joinedAt = joinedAt;
    }

    public Integer getUserId()  {return this.userId;}
    public Integer getBoardId() {return this.boardId;}
    public String  getUsername(){return this.username;}
    public Role    getRole()    {return this.role;}
    public String  getJoinedAt(){return this.joinedAt;}

    public void setUserId(Integer userId){this.userId = userId;}
    public void setBoardId(Integer boardId){this.boardId = boardId;}
    public void setUsername(String username){this.username = username;}
    public void setRole(Role role){this.role = role;}
    public void setJoinedAt(String joinedAt){ this.joinedAt = joinedAt;}
}
