// LaneRepository.java
package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Board;
import com.example.myapp.entity.Lane;

public interface LaneRepository extends JpaRepository<Lane, Integer> {

    // find all lanes on a board
    List<Lane> findByBoard(Board board);
    boolean existsByNameAndBoard(String name,Board board);
}