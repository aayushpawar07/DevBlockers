package com.devblocker.user.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private String userId;
    private String name;
    private String avatarUrl;
    private String teamId;
    private LocalDateTime updatedAt;
}

