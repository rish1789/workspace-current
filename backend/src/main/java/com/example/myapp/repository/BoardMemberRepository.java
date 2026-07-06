package com.example.myapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.myapp.entity.Board;
import com.example.myapp.entity.BoardMember;
import com.example.myapp.entity.AppUser;

/**
 * Repository interface for BoardMember entity.
 *
 * Provides database access for board membership operations.
 * All methods are derived from field names in BoardMember entity.
 *
 * Spring Data JPA generates the SQL automatically — no implementation needed.
 *
 * Derived queries:
 *  - findByBoard        → SELECT * FROM board_members WHERE board_id = ?
 *  - findByBoardAndUser → SELECT * FROM board_members WHERE board_id = ? AND user_id = ?
 *  - existsByBoardAndUser → SELECT COUNT(*) > 0 FROM board_members WHERE board_id = ? AND user_id = ?
 */
public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer> {

    /**
     * Returns all members of a specific board.
     * Used by getMembersByBoardId in BoardService.
     */
    List<BoardMember> findByBoard(Board board);

    /**
     * Finds a specific member in a board.
     * Returns Optional — empty if user is not a member of that board.
     * Used by removeMember, getRole, updateMemberRole in BoardService.
     */
    Optional<BoardMember> findByBoardAndUser(Board board, AppUser user);

    /**
     * Checks if a user is already a member of a board.
     * Used by addMember in BoardService to prevent duplicate membership.
     * Returns true if the user is already a member, false otherwise.
     */
    boolean existsByBoardAndUser(Board board, AppUser user);
}