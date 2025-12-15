package com.devblocker.solution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpvoteRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
}

