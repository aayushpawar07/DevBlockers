package com.devblocker.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncrementReputationRequest {
    
    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    private String reason;
    
    private String source;
}

