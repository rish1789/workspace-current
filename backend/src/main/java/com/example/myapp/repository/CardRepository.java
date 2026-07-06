// CardRepository.java
package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.Lane;

public interface CardRepository extends JpaRepository<Card, Integer> {

    // find all cards in a lane
    List<Card> findByLane(Lane lane);
}