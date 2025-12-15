package com.devblocker.blocker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ResolveBlockerRequest {
    
    @NotNull(message = "Best solution ID is required")
    private UUID bestSolutionId;
}

