package com.example.myapp.repository;

import com.example.myapp.entity.AppUser;

import com.example.myapp.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import com.example.myapp.entity.WorkspaceMember;


public interface WorkspaceMemberRepository extends JpaRepository <WorkspaceMember,Integer> {
        List<WorkspaceMember> findByWorkspace(Workspace workspace);
        List<WorkspaceMember> findByUser(AppUser user);
        Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace,AppUser user);
        boolean existsByWorkspaceAndUser(Workspace workspace,AppUser user);
}


