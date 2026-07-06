package com.example.myapp.dto.respond;

public class  WorkspaceResponse {

    private Integer id;
    private String  name;
    private String  description;
    private Integer createdById;
    private String  createdAt;

    public  WorkspaceResponse() {}

    public  WorkspaceResponse(Integer id, String name, String description, 
                             Integer createdById, String createdAt) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.createdById = createdById;
        this.createdAt   = createdAt;
    }

    public Integer getId()          { return id;          }
    public String  getName()        { return name;        }
    public String  getDescription() { return description; }
    public Integer getCreatedById() { return createdById; }
    public String  getCreatedAt()   { return createdAt;   }

    public void setId(Integer id)                { this.id          = id;          }
    public void setName(String name)             { this.name        = name;        }
    public void setDescription(String desc)      { this.description = desc;        }
    public void setCreatedById(Integer id)       { this.createdById = id;          }
    public void setCreatedAt(String createdAt)   { this.createdAt   = createdAt;   }
}