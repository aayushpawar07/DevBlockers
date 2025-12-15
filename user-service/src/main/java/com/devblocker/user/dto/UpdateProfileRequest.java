package com.devblocker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProfileRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String avatarUrl;
    
    private String bio;
    
    private String location;
    
    private UUID teamId;
}

