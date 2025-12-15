package com.devblocker.solution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateSolutionRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private List<String> mediaUrls;
}

