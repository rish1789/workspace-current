package com.example.myapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.ErrorException.DuplicateResponseException;
import com.example.myapp.ErrorException.ResourceNotFoundException;
import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.CreateLaneRequest;
import com.example.myapp.dto.request.MoveLaneRequest;
import com.example.myapp.dto.request.RenameLaneRequest;
import com.example.myapp.dto.respond.LaneResponse;

import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Board;
import com.example.myapp.entity.Lane;
import com.example.myapp.repository.ActivityLogRepository;
import com.example.myapp.repository.CardLabelRepository;
import com.example.myapp.repository.CardMemberRepository;
import com.example.myapp.repository.CardRepository;
import com.example.myapp.repository.ChecklistItemRepository;
import com.example.myapp.repository.ChecklistRepository;
import com.example.myapp.repository.CommentRepository;
import com.example.myapp.repository.LaneRepository;

/**
 * Service class responsible for all Lane-related business logic.
 *
 * Responsibilities:
 *  - Creates, retrieves, renames, moves, archives, and deletes lanes
 *  - Validates board existence before creating a lane
 *  - Manages lane ordering within a board via position field
 *  - Shifting logic ensures all lane positions stay consistent on move
 *
 * Dependencies:
 *  - BoardRepository — validates board existence
 *  - LaneRepository  — persists and retrieves lanes
 */
@Service
@Transactional
public class LaneService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────

    private final LaneRepository          laneRepository;
    private final Helper                  helper;
    private final AccessControl           accessControl;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public LaneService(LaneRepository laneRepository,CardRepository  cardRepository,
                       CardMemberRepository cardMemberRepository,CardLabelRepository cardLabelRepository,
                       CommentRepository commentRepository,ChecklistRepository checklistRepository,
                       ChecklistItemRepository checklistItemRepository,ActivityLogRepository activityLogRepository,
                       Helper helper,AccessControl accessControl) {
        this.laneRepository          = laneRepository;
        this.helper                  = helper;
        this.accessControl           = accessControl;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────
    private LaneResponse toResponse(Lane lane){
        return new LaneResponse(lane.getId(),lane.getBoard().getId(),lane.getName(),lane.getPosition(),lane.getCreatedAt(),lane.getArchived());
    }

    // ─── LANE CRUD ────────────────────────────────────────────────────────────

    /**
     * Creates a new lane on a board.
     * Validates board exists before creating.
     *
     * @param boardId   the board to create the lane on
     * @param laneName  display name of the lane
     * @param position  zero-based position within the board
     * @return the newly created Lane
     */
    public LaneResponse createLane(CreateLaneRequest request) {
        Board board = helper.resolveBoard(request.getBoardId());
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(board, current);
        if (laneRepository.existsByNameAndBoard(request.getLaneName(), board)) {
           throw new DuplicateResponseException("Name used by other lane");
        }
        return toResponse(laneRepository.save(new Lane(board,request.getLaneName(),request.getPosition())));
    }

    /**
     * Retrieves a lane by its ID.
     *
     * @param laneId the lane ID
     * @return the Lane with that ID
     */
    public LaneResponse getLaneById(Integer laneId) {
        AppUser current = helper.getCurrentUser();
        Lane lane  = helper.resolveLane(laneId);
        accessControl.checkBoardViewAccess(lane.getBoard(), current);
        return toResponse(lane);
    }

    /**
     * Returns all lanes belonging to a specific board.
     *
     * @param boardId the board ID
     * @return list of lanes on that board
     */
    public List<LaneResponse> getLanesByBoardId(Integer boardId) {
        Board board = helper.resolveBoard(boardId);
        AppUser current = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(board, current);
        return laneRepository.findByBoard(board).stream().map(this::toResponse).toList();
    }



    /**
     * Renames a lane.
     *
     * @param laneId     the lane ID
     * @param updateName the new lane name
     */
    public LaneResponse renameLane(Integer laneId,RenameLaneRequest request) {
        Lane lane = helper.resolveLane(laneId);
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(lane.getBoard(), current);
        if (laneRepository.existsByNameAndBoard(request.getLaneName(),lane.getBoard())) {
           throw new DuplicateResponseException("Name used by other lane");
        }
        lane.setName(request.getLaneName());
        return toResponse(laneRepository.save(lane));
        
    }

    /**
     * Moves a lane to a new position and shifts all affected lanes.
     *
     * When moving forward (oldPos < newPos):
     *   all lanes between old and new position shift backward by 1
     *
     * When moving backward (oldPos > newPos):
     *   all lanes between new and old position shift forward by 1
     *
     * All affected lanes are saved in one batch call via saveAll.
     *
     * @param laneId         the lane to move
     * @param updatePosition the new position (must be >= 0 and <= max position)
     */
    public LaneResponse moveLane(Integer laneId,MoveLaneRequest request) {
        Lane lane = helper.resolveLane(laneId);
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(lane.getBoard(), current);
        if (request.getPosition() == null || request.getPosition() < 0)
            throw new IllegalArgumentException("Position must be greater than or equal to zero");

        Integer currentPosition = lane.getPosition();

        // no change needed
        if (currentPosition.equals(request.getPosition())) return toResponse(lane);

        List<Lane> lanes = laneRepository.findByBoard(lane.getBoard());

        if (lanes.isEmpty())
            throw new ResourceNotFoundException("No lanes found for this board");

        int maxPosition = lanes.stream()
                               .mapToInt(Lane::getPosition)
                               .max().orElse(0);

        if (request.getPosition() > maxPosition)
             throw new IllegalArgumentException("Position out of bounds : max allowed is " + maxPosition);

        if (currentPosition < request.getPosition()) {
            // moving forward — shift lanes between old and new position backward
            lanes.stream()
                 .filter(l -> l.getPosition() > currentPosition
                           && l.getPosition() <= request.getPosition()
                           && !l.getId().equals(laneId))
                 .forEach(l -> l.setPosition(l.getPosition() - 1));
        } else {
            // moving backward — shift lanes between new and old position forward
            lanes.stream()
                 .filter(l -> l.getPosition() < currentPosition
                           && l.getPosition() >= request.getPosition()
                           && !l.getId().equals(laneId))
                 .forEach(l -> l.setPosition(l.getPosition() + 1));
        }

        lane.setPosition(request.getPosition());

        // save all affected lanes in one batch call
        laneRepository.saveAll(lanes);

        return toResponse(lane);
    }

    /**
     * Archives a lane — hides it from the board view without deleting it.
     *
     * @param laneId the lane ID
     */
    public LaneResponse archiveLane(Integer laneId) {
        Lane lane = helper.resolveLane(laneId);
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(lane.getBoard(), current);
        lane.archived();
        return toResponse(laneRepository.save(lane));
    }

    /**
     * Unarchives a lane — makes it visible on the board again.
     *
     * @param laneId the lane ID
     */
    public LaneResponse unarchiveLane(Integer laneId) {
        Lane lane = helper.resolveLane(laneId);
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardMemberOrAbove(lane.getBoard(), current);
        lane.unarchived();
        return toResponse(laneRepository.save(lane));
    }

    /**
     * Deletes a lane permanently, along with all its cards and their children.
     *
     * @param laneId the lane ID
     */
    public void deleteLane(Integer laneId) {
        Lane lane = helper.resolveLane(laneId);
        AppUser current = helper.getCurrentUser();
        accessControl.requiredBoardAdmin(lane.getBoard(), current);
        helper.deleteLaneAndChildren(lane);
    }
}