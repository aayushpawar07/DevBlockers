package com.devblocker.notification.listener.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockerCreatedEvent {
    private String blockerId;
    private String title;
    private String description;
    private String createdBy;
    private String assignedTo;
    private String teamId;
    private LocalDateTime createdAt;
}

