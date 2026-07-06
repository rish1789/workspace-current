package com.example.myapp.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.CardLabel;
import com.example.myapp.entity.CardMember;
import com.example.myapp.entity.Label;
import com.example.myapp.entity.Lane;
import com.example.myapp.ErrorException.DuplicateResponseException;
import com.example.myapp.ErrorException.ResourceNotFoundException;
import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.AddCardMemberRequest;
import com.example.myapp.dto.request.AttachLabelRequest;
import com.example.myapp.dto.request.CreateCardRequest;
import com.example.myapp.dto.request.MoveCardRequest;
import com.example.myapp.dto.request.UpdateCardDescriptionRequest;
import com.example.myapp.dto.request.UpdateCardDueDateRequest;
import com.example.myapp.dto.request.UpdateCardTitleRequest;
import com.example.myapp.dto.respond.ActivityLogResponse;
import com.example.myapp.dto.respond.CardLabelResponse;
import com.example.myapp.dto.respond.CardMemberResponse;
import com.example.myapp.dto.respond.CardResponse;
import com.example.myapp.entity.ActivityLog;
import com.example.myapp.entity.AppUser;
import com.example.myapp.repository.ActivityLogRepository;
import com.example.myapp.repository.CardLabelRepository;
import com.example.myapp.repository.CardMemberRepository;
import com.example.myapp.repository.CardRepository;
import com.example.myapp.repository.ChecklistItemRepository;
import com.example.myapp.repository.ChecklistRepository;
import com.example.myapp.repository.CommentRepository;

@Service
@Transactional
public class CardService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────

    private final CardRepository          cardRepository;
    private final CardLabelRepository     cardLabelRepository;
    private final CardMemberRepository    cardMemberRepository;
    private final ActivityLogRepository   activityLogRepository;
    private final Helper                  helper;
    private final AccessControl           accessControl;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public CardService(CardRepository cardRepository,
                       CardLabelRepository cardLabelRepository,
                       CardMemberRepository cardMemberRepository,
                       CommentRepository commentRepository,
                       ChecklistRepository checklistRepository,
                       ChecklistItemRepository checklistItemRepository,
                       ActivityLogRepository activityLogRepository,
                       Helper helper, AccessControl accessControl) {

        this.cardRepository          = cardRepository;
        this.cardLabelRepository     = cardLabelRepository;
        this.cardMemberRepository    = cardMemberRepository;
        this.activityLogRepository   = activityLogRepository;
        this.helper                  = helper;
        this.accessControl           = accessControl;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    


    private CardResponse toCardResponse(Card card) {
        List<CardLabelResponse> labels = cardLabelRepository.findByCard(card)
            .stream()
            .map(this::toLabelResponse)
            .toList();
        return toCardResponse(card, labels);
    }

    // Used when listing a lane's cards — labels are batch-fetched once for
    // the whole lane instead of once per card.
    private CardResponse toCardResponse(Card card, List<CardLabelResponse> labels) {
        List<Integer> assignedUserIds = cardMemberRepository.findByCard(card)
            .stream()
            .map(m -> m.getUser().getId())
            .toList();
        return new CardResponse(card.getId(), card.getTicketId(), card.getLaneId(),
                card.getTitle(), card.getDescription(), card.getPosition(), card.getDueDate(),
                card.isArchived(), card.getCreatedBy().getId(), card.getCreatedAt(),assignedUserIds,labels);
    }

    private CardMemberResponse toMemberResponse(CardMember member) {
        return new CardMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getCard().getId(),
                member.getUser().getUsername(),
                member.getAssignedAt()
        );
    }

    private CardLabelResponse toLabelResponse(CardLabel label) {
        return new CardLabelResponse(label.getId(), label.getCard().getId(), label.getLabel().getId(),
                label.getLabel().getLabelName(), label.getLabel().getColor());
    }

    // ─── CARD CRUD ────────────────────────────────────────────────────────────

    /**
     * Creates a new card in a lane.
     * Logs activity — user is available here.
     */
    public CardResponse createCard(CreateCardRequest request) {
        Lane    lane = helper.resolveLane(request.getLaneId());
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(lane.getBoard(), user);
        Card    card = cardRepository.save(new Card(lane, request.getTitle(), request.getPosition(), user));
        card.generateTicketId();
        cardRepository.save(card);
        // user is available — log activity
        activityLogRepository.save(new ActivityLog(card, user, "Card created: " + card.getTitle()));
        return toCardResponse(card);
    }

    public CardResponse getCardById(Integer cardId) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(),user);
        return toCardResponse(card);
    }

    public List<CardResponse> getCardsByLaneId(Integer laneId) {
        Lane lane = helper.resolveLane(laneId);
        AppUser user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(lane.getBoard(),user);
        List<Card> cards = cardRepository.findByLane(lane);
        Map<Integer, List<CardLabelResponse>> labelsByCardId = cardLabelRepository.findByCardIn(cards)
            .stream()
            .map(this::toLabelResponse)
            .collect(Collectors.groupingBy(CardLabelResponse::getCardId));
        return cards.stream()
            .map(card -> toCardResponse(card, labelsByCardId.getOrDefault(card.getId(), List.of())))
            .toList();
    }

    /**
     * Updates the card title.
     */
    public CardResponse updateTitle(Integer cardId, UpdateCardTitleRequest request) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);
        card.setTitle(request.getTitle());
        Card updated = cardRepository.save(card);
        activityLogRepository.save(new ActivityLog(updated, user, "Title updated to: " + request.getTitle()));
        return toCardResponse(updated);
    }

    /**
     * Updates the card description.
     */
    public CardResponse updateDescription(Integer cardId, UpdateCardDescriptionRequest request) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);
        card.setDescription(request.getDescription());
        Card updated = cardRepository.save(card);
         activityLogRepository.save(new ActivityLog(updated, user, "Description updated"));
        return toCardResponse(updated);
    }

    /**
     * Sets or clears the due date of a card.
     */
    public CardResponse setDueDate(Integer cardId, UpdateCardDueDateRequest request) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);
        card.setDueDate(request.getDate());
        Card updated = cardRepository.save(card);
        activityLogRepository.save(new ActivityLog(updated, user, "Due date set to: " + request.getDate()));
        return toCardResponse(updated);
    }

    /**
     * Archives a card.
     */
    public CardResponse archiveCard(Integer cardId) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);
        card.archived();
        Card updated = cardRepository.save(card);
        activityLogRepository.save(new ActivityLog(updated, user, "Card archived"));
        return toCardResponse(updated);
    }

    /**
     * Unarchives a card.
     */
    public CardResponse unarchiveCard(Integer cardId) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);
        card.unarchived();
        Card updated = cardRepository.save(card);
        activityLogRepository.save(new ActivityLog(updated, user, "Card unarchived"));
        return toCardResponse(updated);
    }

    /**
     * Deletes a card and removes all its members, labels, comments, and checklists.
     * Activity log deleted with card — no point logging a deleted card.
     */
    public void deleteCard(Integer cardId) {
        Card card = helper.resolveCard(cardId);

        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);

        helper.deleteCardAndChildren(card);
    }

    // ─── CARD MOVEMENT ───────────────────────────────────────────────────────

    public CardResponse moveCard(Integer cardId, MoveCardRequest request) {
        Card card       = helper.resolveCard(cardId);
        Lane targetLane = helper.resolveLane(request.getLaneId());
        Lane sourceLane = card.getLane();
        
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),user);
        
        Integer currentPosition = card.getPosition();

        if (!targetLane.getBoard().getId().equals(sourceLane.getBoard().getId()))
            throw new IllegalArgumentException("Cannot move card to a lane on a different board");

        if (request.getLaneId().equals(sourceLane.getId()) && currentPosition.equals(request.getPosition()))
            return toCardResponse(card);

        if (request.getLaneId().equals(sourceLane.getId())) {
            // ─── SAME LANE ───────────────────────────────────────────────
            List<Card> sourceCards = cardRepository.findByLane(sourceLane);

            int maxPosition = sourceCards.stream()
                    .mapToInt(Card::getPosition)
                    .max().orElse(0);

            if (request.getPosition() > maxPosition)
                throw new IllegalArgumentException("Position out of bounds : max allowed is " + maxPosition);

            if (currentPosition > request.getPosition()) {
                sourceCards.stream()
                        .filter(c -> c.getPosition() < currentPosition
                                  && c.getPosition() >= request.getPosition()
                                  && !c.getId().equals(cardId))
                        .forEach(c -> c.setPosition(c.getPosition() + 1));
            } else {
                sourceCards.stream()
                        .filter(c -> c.getPosition() > currentPosition
                                  && c.getPosition() <= request.getPosition()
                                  && !c.getId().equals(cardId))
                        .forEach(c -> c.setPosition(c.getPosition() - 1));
            }

            card.setPosition(request.getPosition());
            cardRepository.saveAll(sourceCards);
            activityLogRepository.save(new ActivityLog(card, user,
            "Card moved to position: " + request.getPosition() + " in lane: " + sourceLane.getName()));
            return toCardResponse(card);

        } else {
            // ─── DIFFERENT LANE ──────────────────────────────────────────
            List<Card> sourceCards = cardRepository.findByLane(sourceLane);
            List<Card> targetCards = cardRepository.findByLane(targetLane);

            int maxPositionInTarget = targetCards.stream()
                    .mapToInt(Card::getPosition)
                    .max().orElse(-1);

            if (request.getPosition() > maxPositionInTarget + 1)
                throw new IllegalArgumentException("Position out of bounds");

            sourceCards.stream()
                    .filter(c -> c.getPosition() > currentPosition && !c.getId().equals(cardId))
                    .forEach(c -> c.setPosition(c.getPosition() - 1));

            targetCards.stream()
                    .filter(c -> c.getPosition() >= request.getPosition())
                    .forEach(c -> c.setPosition(c.getPosition() + 1));

            card.setLane(targetLane);
            card.setPosition(request.getPosition());

            cardRepository.saveAll(sourceCards);
            cardRepository.saveAll(targetCards);
            Card saved = cardRepository.save(card);
            activityLogRepository.save(new ActivityLog(saved, user, "Card moved to lane: " + targetLane.getName()));

            return toCardResponse(saved);
        }
    }

    // ─── MEMBER MANAGEMENT ───────────────────────────────────────────────────

    public CardMemberResponse assignMember(Integer cardId, AddCardMemberRequest request) {
        Card    card = helper.resolveCard(cardId);
        AppUser user = helper.resolveUser(request.getUserId());

        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),current);
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(), user);
        if (cardMemberRepository.existsByCardAndUser(card, user))
            throw new DuplicateResponseException("User is already assigned to this card");
        
        return toMemberResponse(cardMemberRepository.save(new CardMember(card, user)));
    }

    public void removeMember(Integer cardId, Integer userId) {
        Card    card = helper.resolveCard(cardId);
        AppUser user = helper.resolveUser(userId);
        
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),current);
        
        CardMember member = cardMemberRepository.findByCardAndUser(card, user)
                .orElseThrow(() -> new ResourceNotFoundException("User is not assigned to this card"));
        cardMemberRepository.delete(member);
    }

    public List<CardMemberResponse> getCardMembers(Integer cardId) {
        Card card = helper.resolveCard(cardId);
        AppUser current = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(),current);
        return cardMemberRepository.findByCard(card).stream().map(this::toMemberResponse).toList();
    }

    // ─── LABEL MANAGEMENT ────────────────────────────────────────────────────

    public CardLabelResponse attachLabel(Integer cardId, AttachLabelRequest request) {
        Card  card  = helper.resolveCard(cardId);
        Label label = helper.resolveLabel(request.getLabelId());

        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),current);

        if (cardLabelRepository.existsByCardAndLabel(card, label))
            throw new DuplicateResponseException("Label is already attached to this card");

        return toLabelResponse(cardLabelRepository.save(new CardLabel(card, label)));
    }

    public void detachLabel(Integer cardId, Integer labelId) {
        Card  card  = helper.resolveCard(cardId);
        Label label = helper.resolveLabel(labelId);
        
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(),current);
        
        CardLabel cardLabel = cardLabelRepository.findByCardAndLabel(card, label)
                .orElseThrow(() -> new ResourceNotFoundException("Label is not attached to this card"));

        cardLabelRepository.delete(cardLabel);
    }

    public List<CardLabelResponse> getCardLabels(Integer cardId) {
        Card card = helper.resolveCard(cardId);

        AppUser current = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(),current);
        
        return cardLabelRepository.findByCard(card).stream().map(this::toLabelResponse).toList();
    }

    public List<ActivityLogResponse> getActivityLog(Integer cardId) {
        Card card = helper.resolveCard(cardId);
        AppUser current = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(),current);
        
        return activityLogRepository.findByCard(card).stream()
            .map(log -> new ActivityLogResponse(
                    log.getId(),
                    log.getCard().getId(),
                    log.getUser().getId(),
                    log.getAction(),
                    log.getCreatedAt()))
            .toList();
    }
}