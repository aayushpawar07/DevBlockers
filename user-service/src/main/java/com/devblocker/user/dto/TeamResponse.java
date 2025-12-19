package com.devblocker.user.dto;

import com.devblocker.user.model.TeamCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private UUID teamId;
    private String name;
    private TeamCode teamCode;
    private Integer memberCount;
    private LocalDateTime createdAt;
}

