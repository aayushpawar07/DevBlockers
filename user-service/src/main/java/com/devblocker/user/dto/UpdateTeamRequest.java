package com.devblocker.user.dto;

import com.devblocker.user.model.TeamCode;
import lombok.Data;

@Data
public class UpdateTeamRequest {
    
    private String name;
    
    private TeamCode teamCode;
}

