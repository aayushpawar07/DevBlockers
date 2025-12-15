package com.devblocker.notification.service;

import com.devblocker.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${notification.email.critical-events:BLOCKER_CREATED,SOLUTION_ACCEPTED,USER_MENTIONED}")
    private String criticalEventsConfig;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    private Set<Notification.NotificationType> criticalEventTypes;
    
    /**
     * Check if email notifications are enabled and if this event type is critical
     */
    public boolean shouldSendEmail(Notification.NotificationType type) {
        if (!emailEnabled) {
            return false;
        }
        
        if (criticalEventTypes == null) {
            // Parse critical events from configuration
            List<String> criticalEvents = Arrays.asList(criticalEventsConfig.split(","));
            criticalEventTypes = criticalEvents.stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(Notification.NotificationType::valueOf)
                    .collect(Collectors.toSet());
        }
        
        return criticalEventTypes.contains(type);
    }
    
    /**
     * Send email notification
     * Note: In production, you would fetch user email from user-service
     */
    public void sendNotificationEmail(Notification notification, String userEmail) {
        if (!shouldSendEmail(notification.getType())) {
            log.debug("Email not enabled for notification type: {}", notification.getType());
            return;
        }
        
        if (userEmail == null || userEmail.isEmpty()) {
            log.warn("User email not provided for notification: {}", notification.getNotificationId());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());
            
            mailSender.send(message);
            
            log.info("Email notification sent to {} for notification: {}", 
                    userEmail, notification.getNotificationId());
        } catch (Exception e) {
            log.error("Failed to send email notification to {} for notification: {}", 
                    userEmail, notification.getNotificationId(), e);
            // Don't throw - email failure shouldn't break notification creation
        }
    }
}

