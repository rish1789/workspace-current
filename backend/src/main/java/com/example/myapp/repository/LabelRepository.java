// LabelRepository.java
package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Board;
import com.example.myapp.entity.Label;

public interface LabelRepository extends JpaRepository<Label, Integer> {

    // find all labels on a board
    List<Label> findByBoard(Board board);
    boolean existsByNameAndBoard(String labelName, Board board);
}