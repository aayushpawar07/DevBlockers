package com.devblocker.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    
    @Id
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "bio", length = 1000)
    private String bio;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Column(name = "team_id")
    private UUID teamId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;
}

