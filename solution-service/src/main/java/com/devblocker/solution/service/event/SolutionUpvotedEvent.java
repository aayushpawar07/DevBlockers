package com.devblocker.solution.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionUpvotedEvent {
    private String solutionId;
    private String blockerId;
    private String userId;
    private Integer upvotes;
    private LocalDateTime upvotedAt;
}

