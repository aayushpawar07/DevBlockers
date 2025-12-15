package com.devblocker.solution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AcceptSolutionRequest {
    
    @NotNull(message = "User ID is required (user accepting the solution)")
    private UUID userId;
}

