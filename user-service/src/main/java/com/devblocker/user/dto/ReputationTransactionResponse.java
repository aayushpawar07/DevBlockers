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
public class ReputationTransactionResponse {
    private UUID transactionId;
    private UUID userId;
    private Integer points;
    private String reason;
    private String source;
    private LocalDateTime createdAt;
}

