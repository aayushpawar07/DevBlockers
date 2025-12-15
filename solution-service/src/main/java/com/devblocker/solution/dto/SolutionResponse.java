package com.devblocker.solution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionResponse {
    private UUID solutionId;
    private UUID blockerId;
    private UUID userId;
    private String content;
    private List<String> mediaUrls;
    private Integer upvotes;
    private Boolean accepted;
    private LocalDateTime createdAt;
}

