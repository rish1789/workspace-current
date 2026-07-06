package com.example.myapp.HelperFiles;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.myapp.ErrorException.ResourceNotFoundException;
import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Board;
import com.example.myapp.entity.Card;
import com.example.myapp.entity.Checklist;
import com.example.myapp.entity.ChecklistItem;
import com.example.myapp.entity.Comment;
import com.example.myapp.entity.Label;
import com.example.myapp.entity.Lane;
import com.example.myapp.entity.Workspace;
import com.example.myapp.repository.ActivityLogRepository;
import com.example.myapp.repository.AppUserRepository;
import com.example.myapp.repository.BoardMemberRepository;
import com.example.myapp.repository.BoardRepository;
import com.example.myapp.repository.CardLabelRepository;
import com.example.myapp.repository.CardMemberRepository;
import com.example.myapp.repository.CardRepository;
import com.example.myapp.repository.ChecklistItemRepository;
import com.example.myapp.repository.ChecklistRepository;
import com.example.myapp.repository.CommentRepository;
import com.example.myapp.repository.LabelRepository;
import com.example.myapp.repository.LaneRepository;
import com.example.myapp.repository.WorkspaceRepository;

@Component
public class Helper {

    private final AppUserRepository       userRepository;
    private final WorkspaceRepository     workspaceRepository;
    private final BoardRepository         boardRepository;
    private final BoardMemberRepository   boardMemberRepository;
    private final CardRepository          cardRepository;
    private final CardMemberRepository    cardMemberRepository;
    private final CardLabelRepository     cardLabelRepository;
    private final LaneRepository          laneRepository;
    private final LabelRepository         labelRepository;
    private final CommentRepository       commentRepository;
    private final ChecklistRepository     checklistRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final ActivityLogRepository   activityLogRepository;

    public Helper(AppUserRepository userRepository,
                  WorkspaceRepository workspaceRepository,
                  BoardRepository boardRepository,
                  BoardMemberRepository boardMemberRepository,
                  CardRepository cardRepository,
                  CardMemberRepository cardMemberRepository,
                  CardLabelRepository cardLabelRepository,
                  LaneRepository laneRepository,
                  LabelRepository labelRepository,
                  CommentRepository commentRepository,
                  ChecklistRepository checklistRepository,
                  ChecklistItemRepository checklistItemRepository,
                  ActivityLogRepository activityLogRepository) {
        this.userRepository          = userRepository;
        this.workspaceRepository     = workspaceRepository;
        this.boardRepository         = boardRepository;
        this.boardMemberRepository   = boardMemberRepository;
        this.cardRepository          = cardRepository;
        this.cardMemberRepository    = cardMemberRepository;
        this.cardLabelRepository     = cardLabelRepository;
        this.laneRepository          = laneRepository;
        this.labelRepository         = labelRepository;
        this.commentRepository       = commentRepository;
        this.checklistRepository     = checklistRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.activityLogRepository   = activityLogRepository;
    }

    public AppUser resolveUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public AppUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Workspace resolveWorkspace(Integer workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    public Board resolveBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }

    public Label resolveLabel(Integer labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
    }

    public Lane resolveLane(Integer laneId) {
        return laneRepository.findById(laneId)
                .orElseThrow(() -> new ResourceNotFoundException("Lane not found"));
    }

    public Card resolveCard(Integer cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    public Comment resolveComment(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }

    public Checklist resolveChecklist(Integer checklistId) {
        return checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));
    }

    public ChecklistItem resolveChecklistItem(Integer itemId) {
        return checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found"));
    }

    // ─── CASCADE DELETE ──────────────────────────────────────────────────────

    /**
     * Deletes a card and everything that hangs off it: activity logs, card
     * members, card labels, comments, and checklists (with their items).
     */
    public void deleteCardAndChildren(Card card) {
        activityLogRepository.deleteAll(activityLogRepository.findByCard(card));
        cardMemberRepository.deleteAll(cardMemberRepository.findByCard(card));
        cardLabelRepository.deleteAll(cardLabelRepository.findByCard(card));
        commentRepository.deleteAll(commentRepository.findByCard(card));
        List<Checklist> checklists = checklistRepository.findByCard(card);
        for (Checklist checklist : checklists) {
            checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(checklist));
        }
        checklistRepository.deleteAll(checklists);
        cardRepository.delete(card);
    }

    /**
     * Deletes every card in a lane (and their children), then the lane itself.
     */
    public void deleteLaneAndChildren(Lane lane) {
        List<Card> cards = cardRepository.findByLane(lane);
        for (Card card : cards) {
            deleteCardAndChildren(card);
        }
        laneRepository.delete(lane);
    }

    /**
     * Deletes every lane on a board (and their children), then the board's
     * members and labels, then the board itself.
     */
    public void deleteBoardAndChildren(Board board) {
        List<Lane> lanes = laneRepository.findByBoard(board);
        for (Lane lane : lanes) {
            deleteLaneAndChildren(lane);
        }
        boardMemberRepository.deleteAll(boardMemberRepository.findByBoard(board));
        labelRepository.deleteAll(labelRepository.findByBoard(board));
        boardRepository.delete(board);
    }
}
