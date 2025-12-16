package com.devblocker.solution.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "solutions", indexes = {
    @Index(name = "idx_blocker_id", columnList = "blocker_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_accepted", columnList = "accepted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "solution_id", updatable = false, nullable = false)
    private UUID solutionId;
    
    @Column(name = "blocker_id", nullable = false)
    private UUID blockerId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "solution_media", joinColumns = @JoinColumn(name = "solution_id"))
    @Column(name = "media_url")
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();
    
    @Column(name = "upvotes", nullable = false)
    @Builder.Default
    private Integer upvotes = 0;
    
    @Column(name = "accepted", nullable = false)
    @Builder.Default
    private Boolean accepted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (upvotes == null) {
            upvotes = 0;
        }
        if (accepted == null) {
            accepted = false;
        }
    }
}

