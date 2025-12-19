# PowerShell script to start all DevBlocker services
# This script starts all backend services and the frontend

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting DevBlocker Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if MySQL and RabbitMQ are running
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Function to check if a port is in use
function Test-Port {
    param([int]$Port)
    $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue -InformationLevel Quiet
    return $connection
}

# Check MySQL (port 3306)
if (-not (Test-Port -Port 3306)) {
    Write-Host "WARNING: MySQL is not running on port 3306!" -ForegroundColor Red
    Write-Host "Please start MySQL before running the services." -ForegroundColor Yellow
    Write-Host ""
}

# Check RabbitMQ (port 5672)
if (-not (Test-Port -Port 5672)) {
    Write-Host "WARNING: RabbitMQ is not running on port 5672!" -ForegroundColor Red
    Write-Host "Please start RabbitMQ before running the services." -ForegroundColor Yellow
    Write-Host "You can start RabbitMQ with Docker: docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management-alpine" -ForegroundColor Yellow
    Write-Host ""
}

# Get the script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = $scriptDir

Write-Host "Starting backend services..." -ForegroundColor Green
Write-Host ""

# Start Auth Service (port 8081)
Write-Host "Starting Auth Service on port 8081..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\auth'; Write-Host 'Auth Service (Port 8081)' -ForegroundColor Green; mvn spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 3

# Start User Service (port 8082)
Write-Host "Starting User Service on port 8082..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\user-service'; Write-Host 'User Service (Port 8082)' -ForegroundColor Green; mvn spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 3

# Start Blocker Service (port 8083)
Write-Host "Starting Blocker Service on port 8083..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\blocker-service'; Write-Host 'Blocker Service (Port 8083)' -ForegroundColor Green; mvn spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 3

# Start Solution Service (port 8084)
Write-Host "Starting Solution Service on port 8084..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\solution-service'; Write-Host 'Solution Service (Port 8084)' -ForegroundColor Green; mvn spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 3

# Start Comment Service (port 8085)
Write-Host "Starting Comment Service on port 8085..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\comment-service'; Write-Host 'Comment Service (Port 8085)' -ForegroundColor Green; mvn spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 3

# Start Notification Service (port 8086)
Write-Host "Starting Notification Service on port 8086..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\notification-service'; Write-Host 'Notification Service (Port 8086)' -ForegroundColor Green; mvn spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 5

# Start Frontend
Write-Host ""
Write-Host "Starting Frontend on port 3000..." -ForegroundColor Cyan
Write-Host ""

# Check if node_modules exists
if (-not (Test-Path "$rootDir\frontend\node_modules")) {
    Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
    Set-Location "$rootDir\frontend"
    npm install
    Set-Location $rootDir
}

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\frontend'; Write-Host 'Frontend (Port 3000)' -ForegroundColor Green; npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All services are starting!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services:" -ForegroundColor Yellow
Write-Host "  - Auth Service:        http://localhost:8081" -ForegroundColor White
Write-Host "  - User Service:        http://localhost:8082" -ForegroundColor White
Write-Host "  - Blocker Service:     http://localhost:8083" -ForegroundColor White
Write-Host "  - Solution Service:    http://localhost:8084" -ForegroundColor White
Write-Host "  - Comment Service:     http://localhost:8085" -ForegroundColor White
Write-Host "  - Notification Service: http://localhost:8086" -ForegroundColor White
Write-Host "  - Frontend:            http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "Note: Each service is running in a separate PowerShell window." -ForegroundColor Yellow
Write-Host "Close the windows to stop individual services." -ForegroundColor Yellow
Write-Host ""

