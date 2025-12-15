package com.devblocker.auth.controller;

import com.devblocker.auth.dto.AuthResponse;
import com.devblocker.auth.dto.LoginRequest;
import com.devblocker.auth.dto.OtpResponse;
import com.devblocker.auth.dto.RefreshTokenRequest;
import com.devblocker.auth.dto.RegisterRequest;
import com.devblocker.auth.dto.SendOtpRequest;
import com.devblocker.auth.dto.UserResponse;
import com.devblocker.auth.dto.VerifyOtpRequest;
import com.devblocker.auth.model.OtpType;
import com.devblocker.auth.service.AuthService;
import com.devblocker.auth.service.OtpService;
import com.devblocker.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    private final OtpService otpService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates new access and refresh tokens using refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's information")
    public ResponseEntity<UserResponse> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP", description = "Sends OTP code to the provided email")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpType otpType = "REGISTRATION".equalsIgnoreCase(request.getType()) 
                ? OtpType.REGISTRATION 
                : OtpType.LOGIN;
        otpService.generateAndSendOtp(request.getEmail(), otpType);
        return ResponseEntity.ok(OtpResponse.builder()
                .message("OTP sent successfully to your email")
                .success(true)
                .build());
    }
    
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verifies OTP code. For REGISTRATION type, just verifies. For LOGIN type, returns JWT tokens.")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpType otpType = "REGISTRATION".equalsIgnoreCase(request.getType()) 
                ? OtpType.REGISTRATION 
                : OtpType.LOGIN;
        
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getCode(), otpType);
        
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(OtpResponse.builder()
                            .message("Invalid or expired OTP")
                            .success(false)
                            .build());
        }
        
        // For LOGIN type, return auth tokens. For REGISTRATION, just return success
        if (otpType == OtpType.LOGIN) {
            try {
                AuthResponse authResponse = authService.loginWithEmail(request.getEmail());
                return ResponseEntity.ok(authResponse);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(OtpResponse.builder()
                                .message("User not found. Please register first.")
                                .success(false)
                                .build());
            }
        } else {
            // REGISTRATION type - just verify email
            return ResponseEntity.ok(OtpResponse.builder()
                    .message("Email verified successfully")
                    .success(true)
                    .build());
        }
    }
}

