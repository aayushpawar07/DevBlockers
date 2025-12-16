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
@Table(name = "user_teams", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "team_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTeam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_team_id")
    private UUID userTeamId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "team_id", nullable = false)
    private UUID teamId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;
    
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}

