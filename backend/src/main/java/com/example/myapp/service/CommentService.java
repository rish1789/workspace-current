package com.example.myapp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.myapp.entity.Card;
import com.example.myapp.entity.Comment;
import com.example.myapp.ErrorException.AccessDeniedException;
import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.AddCommentRequest;
import com.example.myapp.dto.request.EditCommentRequest;
import com.example.myapp.dto.respond.CommentResponse;
import com.example.myapp.entity.AppUser;
import com.example.myapp.entity.Board;
import com.example.myapp.repository.CommentRepository;


/**
 * Service class responsible for all Comment-related business logic.
 *
 * Responsibilities:
 *  - Adds, retrieves, edits, and deletes comments on cards
 *  - Validates card and user existence before adding a comment
 *
 * Dependencies:
 *  - CardRepository    — validates card existence
 *  - CommentRepository — persists and retrieves comments
 *  - UserRepository    — validates user existence
 */
@Service
@Transactional
public class CommentService {

    // ─── DEPENDENCIES ────────────────────────────────────────────────────────
    private final CommentRepository commentRepository;
    private final Helper              helper;
    private final AccessControl     accessControl;
    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    public CommentService(CommentRepository commentRepository,
                        Helper helper,
                        AccessControl accessControl) {
        this.commentRepository = commentRepository;
        this.helper            = helper;
        this.accessControl     = accessControl;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────
    private CommentResponse toResponse(Comment cmt){
        return new CommentResponse(cmt.getId(),cmt.getCard().getId(),cmt.getUser().getId(),
        cmt.getContent(),cmt.getCreatedAt(),cmt.getUpdatedAt());
    }

    // ─── COMMENT CRUD ────────────────────────────────────────────────────────

    /**
     * Adds a new comment to a card.
     * Validates card and user exist before saving.
     *
     * @param cardId   the card to comment on
     * @param userId   the user adding the comment
     * @param contents the comment text
     * @return the newly created Comment
     */
    public CommentResponse addComment(AddCommentRequest request) {
        Card card = helper.resolveCard(request.getCardId());
        AppUser user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(), user);
        return toResponse(commentRepository.save(new Comment(card, user,request.getContent())));
    }

    /**
     * Retrieves a comment by its ID.
     *
     * @param commentId the comment ID
     * @return the Comment with that ID
     */
    public CommentResponse getCommentById(Integer commentId) {
        Comment comment = helper.resolveComment(commentId);
        Board board  = comment.getCard().getLane().getBoard();
        AppUser user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(board, user);
        return toResponse(comment);
    }

    /**
     * Returns all comments on a specific card.
     *
     * @param cardId the card ID
     * @return list of comments on that card
     */
    public List<CommentResponse> getCommentsByCardId(Integer cardId) {
        Card card = helper.resolveCard(cardId);
        AppUser user = helper.getCurrentUser();
        accessControl.checkBoardViewAccess(card.getLane().getBoard(), user);
        return commentRepository.findByCard(card).stream().map(this::toResponse).toList();
    }

    /**
     * Edits the content of an existing comment.
     * updatedAt is automatically refreshed by Comment.setContent().
     *
     * @param commentId the comment ID
     * @param text      the new content
     */
    public CommentResponse editComment(Integer commentId, EditCommentRequest request) {
      Comment comment = helper.resolveComment(commentId);
      AppUser current = helper.getCurrentUser();

      if (!comment.getUser().getId().equals(current.getId()))
          throw new AccessDeniedException("You can only edit your own comments");

      comment.setContent(request.getContent());
      return toResponse(commentRepository.save(comment));
    }

    /**
     * Deletes a comment permanently.
     *
     * @param commentId the comment ID
     */
    public void deleteComment(Integer commentId) {
      Comment comment = helper.resolveComment(commentId);
      AppUser current = helper.getCurrentUser();
      Board   board   = comment.getCard().getLane().getBoard();

      boolean isAuthor = comment.getUser().getId().equals(current.getId());
      boolean isBoardAdmin = accessControl.isBoardAdmin(board, current);

       if (!isAuthor && !isBoardAdmin)
             throw new AccessDeniedException("You can only delete your own comments");


      commentRepository.delete(comment);
    }
}