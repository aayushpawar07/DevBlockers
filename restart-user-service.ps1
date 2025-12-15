# Script to restart user-service
Write-Host "=== Restarting User Service ===" -ForegroundColor Cyan

# Find the Java process running on port 8082
$process = Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique

if ($process) {
    Write-Host "Found process on port 8082: PID $process" -ForegroundColor Yellow
    Write-Host "Stopping process..." -ForegroundColor Yellow
    Stop-Process -Id $process -Force
    Start-Sleep -Seconds 2
    Write-Host "Process stopped." -ForegroundColor Green
} else {
    Write-Host "No process found on port 8082" -ForegroundColor Yellow
}

Write-Host "`nTo restart the service:" -ForegroundColor Cyan
Write-Host "1. Navigate to user-service directory" -ForegroundColor White
Write-Host "2. Run: mvn spring-boot:run" -ForegroundColor White
Write-Host "   OR if using Docker: docker-compose restart user-service" -ForegroundColor White

