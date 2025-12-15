package com.devblocker.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendOtpEmail(String to, String otpCode, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your OTP Code for " + purpose);
            message.setText(buildOtpEmailBody(otpCode, purpose));
            message.setFrom("shivamsuryawanshi51@gmail.com");
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    
    private String buildOtpEmailBody(String otpCode, String purpose) {
        return String.format(
            "Hello,\n\n" +
            "Your OTP code for %s is: %s\n\n" +
            "This code will expire in 5 minutes.\n\n" +
            "If you didn't request this code, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Auth Service",
            purpose, otpCode
        );
    }
}

