# Quick Authentication Test Script
$baseUrl = "http://localhost:8081/api/v1/auth"
$email = "test@example.com"
$password = "password123"

Write-Host "=== Authentication Service Test ===" -ForegroundColor Cyan

# Step 1: Register
Write-Host "`n[1/5] Registering user..." -ForegroundColor Yellow
$registerBody = @{ email = $email; password = $password } | ConvertTo-Json
try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/register" -Method POST -ContentType "application/json" -Body $registerBody
    Write-Host "âœ“ Registered: $($registerResponse.email)" -ForegroundColor Green
} catch {
    Write-Host "âš  User may exist, continuing..." -ForegroundColor Yellow
}

# Step 2: Login
Write-Host "`n[2/5] Logging in..." -ForegroundColor Yellow
$loginBody = @{ email = $email; password = $password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/login" -Method POST -ContentType "application/json" -Body $loginBody
$accessToken = $loginResponse.accessToken
Write-Host "âœ“ Login successful! Token: $($accessToken.Substring(0, 30))..." -ForegroundColor Green

# Step 3: Get Current User
Write-Host "`n[3/5] Getting current user..." -ForegroundColor Yellow
$headers = @{ Authorization = "Bearer $accessToken" }
$meResponse = Invoke-RestMethod -Uri "$baseUrl/me" -Method GET -Headers $headers
Write-Host "âœ“ User: $($meResponse.email) | Role: $($meResponse.role)" -ForegroundColor Green

# Step 4: Send OTP
Write-Host "`n[4/5] Sending OTP..." -ForegroundColor Yellow
$otpSendBody = @{ email = $email } | ConvertTo-Json
$otpSendResponse = Invoke-RestMethod -Uri "$baseUrl/otp/send" -Method POST -ContentType "application/json" -Body $otpSendBody
Write-Host "âœ“ OTP sent! Check email: $email" -ForegroundColor Green
$otpCode = Read-Host "Enter OTP from email"

# Step 5: Verify OTP
Write-Host "`n[5/5] Verifying OTP..." -ForegroundColor Yellow
$otpVerifyBody = @{ email = $email; code = $otpCode } | ConvertTo-Json
try {
    $otpVerifyResponse = Invoke-RestMethod -Uri "$baseUrl/otp/verify" -Method POST -ContentType "application/json" -Body $otpVerifyBody
    Write-Host "âœ“ 2FA Login successful! Token: $($otpVerifyResponse.accessToken.Substring(0, 30))..." -ForegroundColor Green
} catch {
    Write-Host "âœ— Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
