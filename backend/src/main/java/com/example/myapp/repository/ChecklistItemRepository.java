
package com.example.myapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Checklist;
import com.example.myapp.entity.ChecklistItem;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Integer> {

    // find all items in a checklist
    List<ChecklistItem> findByChecklist(Checklist checklist);
}