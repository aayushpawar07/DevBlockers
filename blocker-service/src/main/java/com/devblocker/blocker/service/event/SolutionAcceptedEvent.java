package com.devblocker.blocker.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionAcceptedEvent {
    private String solutionId;
    private String blockerId;
    private String userId;
    private String acceptedBy;
    private LocalDateTime acceptedAt;
}

