package com.devblocker.notification.listener.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolutionAddedEvent {
    private String solutionId;
    private String blockerId;
    private String userId;
    private String content;
    private LocalDateTime createdAt;
}

