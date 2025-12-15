package com.devblocker.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTeamRequest {
    
    @NotBlank(message = "Team name is required")
    private String name;
}

