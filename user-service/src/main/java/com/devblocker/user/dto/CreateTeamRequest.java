package com.devblocker.user.dto;

import com.devblocker.user.model.TeamCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTeamRequest {
    
    @NotBlank(message = "Team name is required")
    private String name;
    
    @NotNull(message = "Team code is required")
    private TeamCode teamCode;
}

