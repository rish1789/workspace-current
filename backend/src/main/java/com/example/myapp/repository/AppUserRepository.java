package com.example.myapp.repository;


import com.example.myapp.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByUsernameContainingIgnoreCase(String username);
    boolean existsByEmail(String email);

}

