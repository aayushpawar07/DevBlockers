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
public class UserBadgeResponse {
    private UUID userBadgeId;
    private UUID badgeId;
    private String badgeName;
    private String badgeDescription;
    private String iconUrl;
    private LocalDateTime earnedAt;
}

