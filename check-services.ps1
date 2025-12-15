# Quick Service Health Check Script
Write-Host "=== Service Health Check ===" -ForegroundColor Cyan

# Check if ports are in use
Write-Host "`nChecking ports..." -ForegroundColor Yellow
$ports = @(8081, 8082, 8083, 8084, 8085, 8086)
foreach ($port in $ports) {
    $result = netstat -ano | findstr ":$port"
    if ($result) {
        Write-Host "Port $port : IN USE" -ForegroundColor Green
    } else {
        Write-Host "Port $port : NOT IN USE" -ForegroundColor Red
    }
}

# Check RabbitMQ
Write-Host "`nChecking RabbitMQ..." -ForegroundColor Yellow
$rabbitmq = docker ps --filter "name=rabbitmq" --format "{{.Names}}"
if ($rabbitmq) {
    Write-Host "RabbitMQ: RUNNING ($rabbitmq)" -ForegroundColor Green
} else {
    Write-Host "RabbitMQ: NOT RUNNING" -ForegroundColor Red
    Write-Host "Start it with: docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management" -ForegroundColor Yellow
}

# Check MySQL
Write-Host "`nChecking MySQL..." -ForegroundColor Yellow
$mysql = Get-Service -Name "*mysql*" -ErrorAction SilentlyContinue
if ($mysql) {
    $status = ($mysql | Where-Object {$_.Status -eq 'Running'}).Count
    if ($status -gt 0) {
        Write-Host "MySQL: RUNNING" -ForegroundColor Green
    } else {
        Write-Host "MySQL: NOT RUNNING" -ForegroundColor Red
    }
} else {
    Write-Host "MySQL: Service not found (might be running as process)" -ForegroundColor Yellow
}

Write-Host "`n=== Service URLs ===" -ForegroundColor Cyan
Write-Host "Auth: http://localhost:8081"
Write-Host "User: http://localhost:8082"
Write-Host "Blocker: http://localhost:8083"
Write-Host "Solution: http://localhost:8084"
Write-Host "Comment: http://localhost:8085"
Write-Host "Notification: http://localhost:8086"
Write-Host "RabbitMQ Management: http://localhost:15672 (guest/guest)"

