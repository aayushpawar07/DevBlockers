package com.devblocker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTeamRequest {
    
    @NotBlank(message = "Team name is required")
    private String name;
}

