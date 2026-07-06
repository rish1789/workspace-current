package com.example.myapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.CardLabel;
import com.example.myapp.entity.Label;

public interface CardLabelRepository extends JpaRepository<CardLabel, Integer> {

    // check if label is already attached to card
    boolean existsByCardAndLabel(Card card, Label label);

    // find specific card-label link — used by detachLabel
    Optional<CardLabel> findByCardAndLabel(Card card, Label label);

    // find all labels attached to a card
    List<CardLabel> findByCard(Card card);

    // find all labels attached to any of the given cards — batches the
    // per-card lookup used when listing a lane's cards
    List<CardLabel> findByCardIn(List<Card> cards);

}