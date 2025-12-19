package com.devblocker.blocker.dto;

import com.devblocker.blocker.model.BlockerVisibility;
import com.devblocker.blocker.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateBlockerRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Severity is required")
    private Severity severity;
    
    @NotNull(message = "Created by user ID is required")
    private UUID createdBy;
    
    private UUID assignedTo;
    
    private UUID teamId;
    
    private BlockerVisibility visibility;
    
    private UUID orgId;
    
    private UUID groupId;
    
    private List<String> tags;
    
    private List<String> mediaUrls;
}

