package com.example.myapp.repository;

import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {
    List<Workspace> findByCreatedBy(AppUser createdBy);
    boolean existsByNameAndCreatedBy(String name, AppUser createdBy);
}

