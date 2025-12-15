package com.devblocker.solution.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks user upvotes on solutions to ensure idempotency (one vote per user per solution)
 */
@Entity
@Table(name = "solution_upvotes", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"solution_id", "user_id"}),
    indexes = {
        @Index(name = "idx_solution_user", columnList = "solution_id, user_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionUpvote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "upvote_id", updatable = false, nullable = false)
    private UUID upvoteId;
    
    @Column(name = "solution_id", nullable = false)
    private UUID solutionId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

