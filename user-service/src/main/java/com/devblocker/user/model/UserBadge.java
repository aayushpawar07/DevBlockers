package com.devblocker.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_badges", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "badge_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_badge_id")
    private UUID userBadgeId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "badge_id", nullable = false)
    private UUID badgeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", insertable = false, updatable = false)
    private Badge badge;
    
    @CreationTimestamp
    @Column(name = "earned_at", nullable = false, updatable = false)
    private LocalDateTime earnedAt;
}

