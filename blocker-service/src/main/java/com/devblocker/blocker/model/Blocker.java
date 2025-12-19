package com.devblocker.blocker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "blockers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blocker {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "blocker_id")
    private UUID blockerId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 5000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockerStatus status = BlockerStatus.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    
    @Column(name = "assigned_to")
    private UUID assignedTo;
    
    @Column(name = "team_id")
    private UUID teamId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockerVisibility visibility = BlockerVisibility.PUBLIC;
    
    @Column(name = "org_id")
    private UUID orgId;
    
    @Column(name = "group_id")
    private UUID groupId;
    
    @Column(name = "best_solution_id")
    private UUID bestSolutionId;
    
    @ElementCollection
    @CollectionTable(name = "blocker_tags", joinColumns = @JoinColumn(name = "blocker_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "blocker_media", joinColumns = @JoinColumn(name = "blocker_id"))
    @Column(name = "media_url")
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}

