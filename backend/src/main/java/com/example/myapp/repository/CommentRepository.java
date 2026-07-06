package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.Comment;

public interface CommentRepository extends JpaRepository <Comment,Integer> {
     List<Comment>findByCard(Card card);
}
