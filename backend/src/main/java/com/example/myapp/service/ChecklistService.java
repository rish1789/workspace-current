package com.example.myapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.AddChecklistItemRequest;
import com.example.myapp.dto.request.CreateChecklistRequest;
import com.example.myapp.dto.request.RenameChecklistRequest;
import com.example.myapp.dto.request.UpdateChecklistItemContentRequest;
import com.example.myapp.dto.respond.ChecklistItemResponse;
import com.example.myapp.dto.respond.ChecklistResponse;
import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Board;
import com.example.myapp.entity.Card;
import com.example.myapp.entity.Checklist;
import com.example.myapp.entity.ChecklistItem;
import com.example.myapp.repository.ChecklistItemRepository;
import com.example.myapp.repository.ChecklistRepository;

@Service
@Transactional
public class ChecklistService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────

    private final ChecklistRepository     checklistRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final Helper                  helper;
    private final AccessControl           accessControl;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public ChecklistService(ChecklistRepository checklistRepository,
                            ChecklistItemRepository checklistItemRepository,
                            Helper helper,
                            AccessControl accessControl) {
        this.checklistRepository     = checklistRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.helper                  = helper;
        this.accessControl           = accessControl;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    private ChecklistResponse toListResponse(Checklist checklist) {
        return new ChecklistResponse(checklist.getId(), checklist.getCard().getId(), checklist.getTitle());
    }

    private ChecklistItemResponse toItemResponse(ChecklistItem item) {
        return new ChecklistItemResponse(item.getId(), item.getChecklist().getId(),
                item.getContent(), item.isDone(), item.getPosition());
    }

    private Board boardOf(Checklist checklist) {
        return checklist.getCard().getLane().getBoard();
    }

    private Board boardOf(ChecklistItem item) {
        return item.getChecklist().getCard().getLane().getBoard();
    }

    // ─── CHECKLIST CRUD ──────────────────────────────────────────────────────

    /**
     * Creates a new checklist on a card.
     */
    public ChecklistResponse createChecklist(CreateChecklistRequest request) {
        Card    card = helper.resolveCard(request.getCardId());
        AppUser user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(), user);
        return toListResponse(checklistRepository.save(new Checklist(card, request.getTitle())));
    }

    /**
     * Retrieves a checklist by its ID.
     */
    public ChecklistResponse getChecklistById(Integer checklistId) {
        Checklist checklist = helper.resolveChecklist(checklistId);
        AppUser   user      = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(boardOf(checklist), user);
        return toListResponse(checklist);
    }

    /**
     * Returns all checklists on a specific card.
     */
    public List<ChecklistResponse> getChecklistsByCardId(Integer cardId) {
        Card    card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(), user);
        return checklistRepository.findByCard(card).stream().map(this::toListResponse).toList();
    }

    /**
     * Renames a checklist.
     */
    public ChecklistResponse renameChecklist(Integer checklistId, RenameChecklistRequest request) {
        Checklist checklist = helper.resolveChecklist(checklistId);
        AppUser   user      = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(checklist), user);
        checklist.setTitle(request.getTitle());
        return toListResponse(checklistRepository.save(checklist));
    }

    /**
     * Deletes a checklist and all its items.
     * Items are deleted first to avoid foreign key violations.
     */
    public void deleteChecklist(Integer checklistId) {
        Checklist checklist = helper.resolveChecklist(checklistId);
        AppUser   user      = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(checklist), user);

        checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(checklist));
        checklistRepository.delete(checklist);
    }

    // ─── ITEM CRUD ───────────────────────────────────────────────────────────

    /**
     * Adds a new item to a checklist.
     */
    public ChecklistItemResponse addItem(Integer checklistId, AddChecklistItemRequest request) {
        Checklist checklist = helper.resolveChecklist(checklistId);
        AppUser   user      = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(checklist), user);
        return toItemResponse(checklistItemRepository.save(
                new ChecklistItem(checklist, request.getContent(), request.getPosition())));
    }

    /**
     * Retrieves a checklist item by its ID.
     */
    public ChecklistItemResponse getChecklistItemById(Integer itemId) {
        ChecklistItem item = helper.resolveChecklistItem(itemId);
        AppUser       user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(boardOf(item), user);
        return toItemResponse(item);
    }

    /**
     * Returns all items in a specific checklist.
     */
    public List<ChecklistItemResponse> getChecklistItemsByChecklistId(Integer checklistId) {
        Checklist checklist = helper.resolveChecklist(checklistId);
        AppUser   user      = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(boardOf(checklist), user);
        return checklistItemRepository.findByChecklist(checklist).stream().map(this::toItemResponse).toList();
    }

    /**
     * Updates the content of a checklist item.
     */
    public ChecklistItemResponse updateItemContent(Integer itemId, UpdateChecklistItemContentRequest request) {
        ChecklistItem item = helper.resolveChecklistItem(itemId);
        AppUser       user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(item), user);
        item.setContent(request.getContent());
        return toItemResponse(checklistItemRepository.save(item));
    }

    /**
     * Marks a checklist item as completed.
     */
    public ChecklistItemResponse completeItem(Integer itemId) {
        ChecklistItem item = helper.resolveChecklistItem(itemId);
        AppUser       user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(item), user);
        item.markDone();
        return toItemResponse(checklistItemRepository.save(item));
    }

    /**
     * Marks a checklist item as not completed.
     */
    public ChecklistItemResponse uncompleteItem(Integer itemId) {
        ChecklistItem item = helper.resolveChecklistItem(itemId);
        AppUser       user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(item), user);
        item.markUndone();
        return toItemResponse(checklistItemRepository.save(item));
    }

    /**
     * Deletes a checklist item permanently.
     */
    public void deleteItem(Integer itemId) {
        ChecklistItem item = helper.resolveChecklistItem(itemId);
        AppUser       user = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(boardOf(item), user);
        checklistItemRepository.delete(item);
    }
}