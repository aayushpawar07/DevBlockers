package com.devblocker.auth.service;

import com.devblocker.auth.dto.AuthResponse;
import com.devblocker.auth.dto.LoginRequest;
import com.devblocker.auth.dto.RefreshTokenRequest;
import com.devblocker.auth.model.User;
import com.devblocker.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        String accessToken = jwtService.generateAccessToken(
                user.getUserId(), 
                user.getEmail(), 
                user.getRole().name()
        );
        
        String refreshToken = jwtService.generateRefreshToken(
                user.getUserId(), 
                user.getEmail()
        );
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtService.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }
        
        String email = jwtService.extractEmail(refreshToken);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(
                user.getUserId(), 
                user.getEmail(), 
                user.getRole().name()
        );
        
        String newRefreshToken = jwtService.generateRefreshToken(
                user.getUserId(), 
                user.getEmail()
        );
        
        log.info("Tokens refreshed for user: {}", user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
    }
    
    public String getEmailFromToken(String token) {
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        return jwtService.extractEmail(token);
    }
    
    public AuthResponse loginWithEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String accessToken = jwtService.generateAccessToken(
                user.getUserId(), 
                user.getEmail(), 
                user.getRole().name()
        );
        
        String refreshToken = jwtService.generateRefreshToken(
                user.getUserId(), 
                user.getEmail()
        );
        
        log.info("User logged in successfully via OTP: {}", user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
    }
}

