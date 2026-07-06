package com.example.myapp.dto.respond;

import java.util.List;

public class CardResponse {
        private Integer id;
        private String  fullId;
        private Integer laneId;
        private String  title;
        private String  description;
        private Integer position;
        private String  dueDate;
        private boolean isArchived;
        private Integer createdBy;
        private String  createdAt;
        private List<Integer> assignedUserIds;
        private List<CardLabelResponse> labels;

        public CardResponse(){}
        public CardResponse(Integer id,String fullId,Integer laneId,String title,String description,
                            Integer position,String dueDate,boolean isArchived,Integer createdBy,String createdAt,
                            List<Integer> assignedUserIds,List<CardLabelResponse> labels){

            this.id = id;
            this.fullId = fullId;
            this.laneId = laneId;
            this.title = title;
            this.description = description;
            this.position = position;
            this.dueDate = dueDate;
            this.isArchived = isArchived;
            this.createdBy  = createdBy;
            this.createdAt = createdAt;
            this.assignedUserIds = assignedUserIds;
            this.labels = labels;

        }

        public Integer getId(){return id;}
        public String  getFullId(){return fullId;}
        public Integer getLaneId(){return laneId;}
        public String  getTitle(){return title;}
        public String  getDescription(){return description;}
        public Integer getPosition(){return position;}
        public String  getDueDate(){return dueDate;}
        public boolean isArchived(){return isArchived;}
        public Integer createdBy(){return createdBy;}
        public String  createdAt(){return createdAt;}
        public List<Integer> getAssignedUserIds(){return assignedUserIds;}
        public List<CardLabelResponse> getLabels(){return labels;}
}
