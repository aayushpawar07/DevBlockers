package com.devblocker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBadgeRequest {
    
    @NotBlank(message = "Badge name is required")
    private String name;
    
    private String description;
    
    private String iconUrl;
    
    private Integer reputationThreshold;
}

