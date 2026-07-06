// ChecklistRepository.java — correct, no changes needed
package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.Checklist;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {

    // find all checklists on a card
    List<Checklist> findByCard(Card card);
}