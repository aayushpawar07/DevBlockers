package com.devblocker.user.dto;

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
public class ProfileResponse {
    private UUID userId;
    private String name;
    private String avatarUrl;
    private String bio;
    private String location;
    private UUID teamId;
    private String teamName;
    private Integer solutionsCount;
    private Integer acceptedSolutionsCount;
    private Integer blockersCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

