# Authentication Service Testing Guide

Complete step-by-step guide to test registration, login, and 2FA (OTP) functionality.

## Prerequisites

1. **Start the application:**
   ```powershell
   cd "C:\Users\Aayush Paradkar\Downloads\auth\auth"
   mvn spring-boot:run
   ```

2. **Verify services are running:**
   - Application: http://localhost:8081
   - Swagger UI: http://localhost:8081/swagger-ui.html
   - Health Check: http://localhost:8081/actuator/health

---

## Method 1: Using Swagger UI (Easiest)

### Step 1: Open Swagger UI
Navigate to: **http://localhost:8081/swagger-ui.html**

### Step 2: Register a User
1. Find **`POST /api/v1/auth/register`**
2. Click **"Try it out"**
3. Enter the request body:
   ```json
   {
     "email": "test@example.com",
     "password": "password123"
   }
   ```
4. Click **"Execute"**
5. Note the response (should return user details with status 201)

### Step 3: Regular Login
1. Find **`POST /api/v1/auth/login`**
2. Click **"Try it out"**
3. Enter the request body:
   ```json
   {
     "email": "test@example.com",
     "password": "password123"
   }
   ```
4. Click **"Execute"**
5. **Copy the `accessToken` and `refreshToken`** from the response

### Step 4: Test Protected Endpoint (/me)
1. Find **`GET /api/v1/auth/me`**
2. Click **"Try it out"**
3. Click **"Authorize"** button at the top
4. Enter: `Bearer <your-access-token>` (replace with actual token)
5. Click **"Authorize"** then **"Close"**
6. Click **"Execute"**
7. Should return your user information

### Step 5: Test 2FA (OTP Login)
1. **Send OTP:**
   - Find **`POST /api/v1/auth/otp/send`**
   - Click **"Try it out"**
   - Enter:
     ```json
     {
       "email": "test@example.com"
     }
     ```
   - Click **"Execute"**
   - **Check your email** for the 6-digit OTP code

2. **Verify OTP:**
   - Find **`POST /api/v1/auth/otp/verify`**
   - Click **"Try it out"**
   - Enter:
     ```json
     {
       "email": "test@example.com",
       "code": "123456"
     }
     ```
   - Replace `123456` with the actual OTP from your email
   - Click **"Execute"**
   - Should return JWT tokens (accessToken and refreshToken)

---

## Method 2: Using PowerShell (Command Line)

### Step 1: Register a User
```powershell
$registerBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $registerBody

Write-Host "User registered:" -ForegroundColor Green
$registerResponse | ConvertTo-Json
```

### Step 2: Regular Login
```powershell
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

$accessToken = $loginResponse.accessToken
$refreshToken = $loginResponse.refreshToken

Write-Host "Login successful!" -ForegroundColor Green
Write-Host "Access Token: $($accessToken.Substring(0, 50))..." -ForegroundColor Yellow
```

### Step 3: Test Protected Endpoint (/me)
```powershell
$headers = @{
    Authorization = "Bearer $accessToken"
}

$meResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/me" `
    -Method GET `
    -Headers $headers

Write-Host "Current user:" -ForegroundColor Green
$meResponse | ConvertTo-Json
```

### Step 4: Test 2FA (OTP Login)

**4a. Send OTP:**
```powershell
$otpSendBody = @{
    email = "test@example.com"
} | ConvertTo-Json

$otpSendResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/otp/send" `
    -Method POST `
    -ContentType "application/json" `
    -Body $otpSendBody

Write-Host "OTP sent! Check your email." -ForegroundColor Green
$otpSendResponse | ConvertTo-Json
```

**4b. Verify OTP (replace 123456 with actual OTP from email):**
```powershell
$otpVerifyBody = @{
    email = "test@example.com"
    code = "123456"  # Replace with actual OTP from email
} | ConvertTo-Json

$otpVerifyResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/otp/verify" `
    -Method POST `
    -ContentType "application/json" `
    -Body $otpVerifyBody

Write-Host "OTP verified! Tokens received:" -ForegroundColor Green
$otpVerifyResponse | ConvertTo-Json

# Save new tokens
$accessToken2FA = $otpVerifyResponse.accessToken
$refreshToken2FA = $otpVerifyResponse.refreshToken
```

---

## Method 3: Using cURL (if available)

### Step 1: Register
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

### Step 2: Login
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

### Step 3: Get Current User (replace TOKEN with actual token)
```bash
curl -X GET http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### Step 4: Send OTP
```bash
curl -X POST http://localhost:8081/api/v1/auth/otp/send \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\"}"
```

### Step 5: Verify OTP (replace 123456 with actual OTP)
```bash
curl -X POST http://localhost:8081/api/v1/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"code\":\"123456\"}"
```

---

## Complete Test Script (PowerShell)

Save this as `test-auth.ps1` and run it:

```powershell
# Configuration
$baseUrl = "http://localhost:8081/api/v1/auth"
$email = "test@example.com"
$password = "password123"

Write-Host "=== Authentication Service Test ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Register
Write-Host "Step 1: Registering user..." -ForegroundColor Yellow
$registerBody = @{ email = $email; password = $password } | ConvertTo-Json
try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/register" `
        -Method POST -ContentType "application/json" -Body $registerBody
    Write-Host "✓ User registered: $($registerResponse.email)" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "⚠ User may already exist, continuing..." -ForegroundColor Yellow
    } else {
        throw
    }
}

# Step 2: Login
Write-Host "`nStep 2: Logging in..." -ForegroundColor Yellow
$loginBody = @{ email = $email; password = $password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/login" `
    -Method POST -ContentType "application/json" -Body $loginBody
$accessToken = $loginResponse.accessToken
Write-Host "✓ Login successful!" -ForegroundColor Green
Write-Host "  Access Token: $($accessToken.Substring(0, 50))..." -ForegroundColor Gray

# Step 3: Get Current User
Write-Host "`nStep 3: Getting current user..." -ForegroundColor Yellow
$headers = @{ Authorization = "Bearer $accessToken" }
$meResponse = Invoke-RestMethod -Uri "$baseUrl/me" `
    -Method GET -Headers $headers
Write-Host "✓ Current user: $($meResponse.email) (Role: $($meResponse.role))" -ForegroundColor Green

# Step 4: Send OTP
Write-Host "`nStep 4: Sending OTP..." -ForegroundColor Yellow
$otpSendBody = @{ email = $email } | ConvertTo-Json
$otpSendResponse = Invoke-RestMethod -Uri "$baseUrl/otp/send" `
    -Method POST -ContentType "application/json" -Body $otpSendBody
Write-Host "✓ OTP sent! Check your email: $email" -ForegroundColor Green
Write-Host "  Please enter the OTP code from your email:" -ForegroundColor Yellow
$otpCode = Read-Host "OTP Code"

# Step 5: Verify OTP
Write-Host "`nStep 5: Verifying OTP..." -ForegroundColor Yellow
$otpVerifyBody = @{ email = $email; code = $otpCode } | ConvertTo-Json
try {
    $otpVerifyResponse = Invoke-RestMethod -Uri "$baseUrl/otp/verify" `
        -Method POST -ContentType "application/json" -Body $otpVerifyBody
    Write-Host "✓ OTP verified! 2FA login successful!" -ForegroundColor Green
    Write-Host "  New Access Token: $($otpVerifyResponse.accessToken.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ OTP verification failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
```

---

## Expected Responses

### Register Response (201 Created):
```json
{
  "userId": "uuid-here",
  "email": "test@example.com",
  "role": "USER",
  "createdAt": "2024-12-09T..."
}
```

### Login Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
  "tokenType": "Bearer"
}
```

### OTP Send Response (200 OK):
```json
{
  "message": "OTP sent successfully to your email",
  "success": true
}
```

### OTP Verify Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
  "tokenType": "Bearer"
}
```

---

## Troubleshooting

### Issue: "User already exists"
- User is already registered, skip registration step

### Issue: "Invalid email or password"
- Check email and password are correct
- User must be registered first

### Issue: "Invalid or expired OTP"
- OTP expires in 5 minutes
- Each OTP can only be used once
- Request a new OTP

### Issue: "User not found" during OTP verify
- User must be registered before using OTP login
- Register first using `/api/v1/auth/register`

### Issue: Email not received
- Check spam folder
- Verify email configuration in `application.properties`
- Check application logs for email errors

---

## Quick Test Checklist

- [ ] Application is running on port 8081
- [ ] MySQL database is accessible
- [ ] RabbitMQ is running (optional, for events)
- [ ] Email configuration is correct
- [ ] Registered a test user
- [ ] Regular login works
- [ ] /me endpoint works with token
- [ ] OTP sent successfully
- [ ] OTP received in email
- [ ] OTP verification works
- [ ] 2FA login returns tokens

