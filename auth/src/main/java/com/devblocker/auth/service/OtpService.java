package com.devblocker.auth.service;

import com.devblocker.auth.model.Otp;
import com.devblocker.auth.model.OtpType;
import com.devblocker.auth.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final SecureRandom random = new SecureRandom();
    
    @Value("${app.session.timeout.minutes:5}")
    private int sessionTimeoutMinutes;
    
    @Transactional
    public String generateAndSendOtp(String email, OtpType type) {
        // Invalidate previous unused OTPs for this email and type
        otpRepository.markAllAsUsed(email, type);
        
        // Generate new OTP
        String otpCode = generateOtp();
        
        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        
        // Save OTP
        Otp otp = Otp.builder()
                .email(email)
                .code(otpCode)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .build();
        
        otpRepository.save(otp);
        
        // Send email
        String purpose = getPurposeForType(type);
        emailService.sendOtpEmail(email, otpCode, purpose);
        
        log.info("OTP generated and sent to: {} for type: {}", email, type);
        
        return otpCode;
    }
    
    @Transactional
    public boolean verifyOtp(String email, String code, OtpType type) {
        Optional<Otp> otpOpt = otpRepository.findByEmailAndCodeAndTypeAndUsedFalse(email, code, type);
        
        if (otpOpt.isEmpty()) {
            log.warn("Invalid OTP attempt for email: {}", email);
            return false;
        }
        
        Otp otp = otpOpt.get();
        
        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            log.warn("Expired OTP used for email: {}", email);
            otp.setUsed(true);
            otpRepository.save(otp);
            return false;
        }
        
        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);
        
        log.info("OTP verified successfully for email: {}", email);
        return true;
    }
    
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    private String getPurposeForType(OtpType type) {
        return switch (type) {
            case REGISTRATION -> "Registration";
            case LOGIN -> "Login";
            case PASSWORD_RESET -> "Password Reset";
        };
    }
    
    // Clean up expired OTPs every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.debug("Cleaned up expired OTPs");
    }
}

