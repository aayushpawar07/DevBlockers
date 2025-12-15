package com.devblocker.blocker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateBestSolutionRequest {
    
    @NotNull(message = "Best solution ID is required")
    private UUID bestSolutionId;
}

