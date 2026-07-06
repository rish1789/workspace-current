package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Board;
import com.example.myapp.entity.Workspace;

public interface BoardRepository extends JpaRepository<Board, Integer> {

    // find all boards in a workspace
    List<Board> findByWorkspace(Workspace workspace);
    boolean existsByNameAndWorkspace(String name, Workspace workspace);
}