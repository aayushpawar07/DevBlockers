package com.devblocker.blocker.dto;

import com.devblocker.blocker.model.Severity;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateBlockerRequest {
    
    private String title;
    
    private String description;
    
    private Severity severity;
    
    private UUID assignedTo;
    
    private UUID teamId;
    
    private List<String> tags;
}

