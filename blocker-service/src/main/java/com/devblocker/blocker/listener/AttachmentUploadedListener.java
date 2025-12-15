package com.devblocker.blocker.listener;

import com.devblocker.blocker.repository.BlockerRepository;
import com.devblocker.blocker.service.event.AttachmentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentUploadedListener {
    
    private final BlockerRepository blockerRepository;
    
    @RabbitListener(queues = "attachment.uploaded.queue")
    public void handleAttachmentUploaded(AttachmentUploadedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            log.info("Received AttachmentUploaded event for blocker: {}", blockerId);
            
            // Validate blocker exists
            if (!blockerRepository.findByBlockerId(blockerId).isPresent()) {
                log.warn("Blocker not found: {}", blockerId);
                return;
            }
            
            // Attach file to blocker (you can extend Blocker entity to store attachment references)
            // For now, just log the attachment
            log.info("Attachment {} uploaded for blocker: {} by user: {}", 
                    event.getFileName(), blockerId, event.getUploadedBy());
            
            // TODO: Store attachment reference in blocker or separate attachment table
            
        } catch (Exception e) {
            log.error("Failed to process AttachmentUploaded event: {}", event, e);
            // Consider implementing retry logic or dead letter queue
        }
    }
}

