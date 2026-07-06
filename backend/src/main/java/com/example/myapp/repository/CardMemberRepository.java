// CardMemberRepository.java
package com.example.myapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.CardMember;
import com.example.myapp.entity.AppUser;

public interface CardMemberRepository extends JpaRepository<CardMember, Integer> {

    // find all members assigned to a card
    List<CardMember> findByCard(Card card);

    // find specific card-user assignment
    Optional<CardMember> findByCardAndUser(Card card, AppUser user);

    // check if user is already assigned — used for duplicate check
    boolean existsByCardAndUser(Card card, AppUser user);
}