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
public class AttachmentUploadedEvent {
    private String attachmentId;
    private String blockerId;
    private String fileName;
    private String fileUrl;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}

