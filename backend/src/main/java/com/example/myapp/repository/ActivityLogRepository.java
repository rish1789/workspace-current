package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.ActivityLog;
import com.example.myapp.entity.Card;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Integer>{
    List<ActivityLog>findByCard(Card card);
}
