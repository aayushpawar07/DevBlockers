package com.devblocker.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponse {
    private UUID badgeId;
    private String name;
    private String description;
    private String iconUrl;
    private Integer reputationThreshold;
    private LocalDateTime createdAt;
}

