package com.devblocker.comment.repository;

import com.devblocker.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    
    /**
     * Find all top-level comments for a blocker (no parent)
     * Ordered by creation date (oldest first)
     */
    List<Comment> findByBlockerIdAndParentCommentIdIsNullOrderByCreatedAtAsc(UUID blockerId);
    
    /**
     * Find all replies to a specific comment
     * Ordered by creation date (oldest first)
     */
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
    
    /**
     * Find all comments for a blocker (including replies)
     * Ordered by creation date (oldest first)
     */
    List<Comment> findByBlockerIdOrderByCreatedAtAsc(UUID blockerId);
    
    Optional<Comment> findByCommentId(UUID commentId);
    
    boolean existsByCommentId(UUID commentId);
    
    long countByBlockerId(UUID blockerId);
    
    /**
     * Count replies for a specific comment
     */
    long countByParentCommentId(UUID parentCommentId);
}

