package com.devblocker.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AwardBadgeRequest {
    
    @NotNull(message = "Badge ID is required")
    private UUID badgeId;
}

