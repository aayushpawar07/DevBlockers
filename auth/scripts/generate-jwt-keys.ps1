# Generate RSA key pair for JWT signing (RS256)
# This script helps generate 2048-bit RSA key pairs for Windows

$KeyDir = "src\main\resources\jwt"
$PrivateKeyPath = "$KeyDir\private-key.pem"
$PublicKeyPath = "$KeyDir\public-key.pem"

# Create directory if it doesn't exist
if (-not (Test-Path $KeyDir)) {
    New-Item -ItemType Directory -Path $KeyDir -Force | Out-Null
}

Write-Host "Generating RSA keys for JWT signing..." -ForegroundColor Cyan
Write-Host ""

# Try OpenSSL first (if available)
if (Get-Command openssl -ErrorAction SilentlyContinue) {
    Write-Host "Using OpenSSL..." -ForegroundColor Green
    openssl genpkey -algorithm RSA -out $PrivateKeyPath -pkeyopt rsa_keygen_bits:2048
    openssl rsa -pubout -in $PrivateKeyPath -out $PublicKeyPath
    Write-Host ""
    Write-Host "RSA keys generated successfully!" -ForegroundColor Green
    Write-Host "Private key: $PrivateKeyPath"
    Write-Host "Public key: $PublicKeyPath"
    exit 0
}

# Try Git Bash (common on Windows)
$GitBashPath = "${env:ProgramFiles}\Git\bin\bash.exe"
if (Test-Path $GitBashPath) {
    Write-Host "Using Git Bash..." -ForegroundColor Green
    $CurrentDir = (Get-Location).Path
    $PrivateKeyFullPath = (Resolve-Path $KeyDir -ErrorAction SilentlyContinue).Path
    if (-not $PrivateKeyFullPath) {
        $PrivateKeyFullPath = Join-Path $CurrentDir $KeyDir
    }
    $PrivateKeyFullPath = Join-Path $PrivateKeyFullPath "private-key.pem"
    $PublicKeyFullPath = Join-Path $PrivateKeyFullPath.Replace("private-key.pem", "") "public-key.pem"
    
    # Convert Windows path to Git Bash path format
    $BashPath = $CurrentDir -replace '\\', '/' -replace '^([A-Z]):', '/$1' -replace ':', ''
    $BashPrivateKey = ($PrivateKeyPath -replace '\\', '/')
    $BashPublicKey = ($PublicKeyPath -replace '\\', '/')
    
    $ExitCode1 = & $GitBashPath -c "cd '$BashPath' && openssl genpkey -algorithm RSA -out $BashPrivateKey -pkeyopt rsa_keygen_bits:2048; exit `$?"
    $ExitCode2 = & $GitBashPath -c "cd '$BashPath' && openssl rsa -pubout -in $BashPrivateKey -out $BashPublicKey; exit `$?"
    
    if (Test-Path $PrivateKeyPath) {
        Write-Host ""
        Write-Host "RSA keys generated successfully!" -ForegroundColor Green
        Write-Host "Private key: $PrivateKeyPath"
        Write-Host "Public key: $PublicKeyPath"
        exit 0
    } elseif ($ExitCode1 -eq 0 -or $ExitCode2 -eq 0) {
        Write-Host "Keys may have been generated. Please check: $KeyDir" -ForegroundColor Yellow
    }
}

# If we get here, OpenSSL is not available
Write-Host "OpenSSL not found. Please use one of these options:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Option 1: Install OpenSSL for Windows" -ForegroundColor Cyan
Write-Host "  Download from: https://slproweb.com/products/Win32OpenSSL.html"
Write-Host "  Or use Chocolatey: choco install openssl"
Write-Host ""
Write-Host "Option 2: Use Git Bash (if you have Git installed)" -ForegroundColor Cyan
Write-Host "  Open Git Bash and run:"
Write-Host "  cd '$(Resolve-Path .)'"
Write-Host "  openssl genpkey -algorithm RSA -out $PrivateKeyPath -pkeyopt rsa_keygen_bits:2048"
Write-Host "  openssl rsa -pubout -in $PrivateKeyPath -out $PublicKeyPath"
Write-Host ""
Write-Host "Option 3: Use WSL (Windows Subsystem for Linux)" -ForegroundColor Cyan
Write-Host "  wsl openssl genpkey -algorithm RSA -out $PrivateKeyPath -pkeyopt rsa_keygen_bits:2048"
Write-Host "  wsl openssl rsa -pubout -in $PrivateKeyPath -out $PublicKeyPath"
Write-Host ""
Write-Host "Option 4: Use online RSA key generator" -ForegroundColor Cyan
Write-Host "  Visit: https://8gwifi.org/rsakeygenerator.jsp"
Write-Host "  Generate 2048-bit RSA keys and save as PEM format"
Write-Host ""
Write-Host "Option 5: Use Docker (if you have Docker installed)" -ForegroundColor Cyan
Write-Host "  docker run --rm -v `"${PWD}:${PWD}`" -w ${PWD} alpine/openssl genpkey -algorithm RSA -out $PrivateKeyPath -pkeyopt rsa_keygen_bits:2048"
Write-Host "  docker run --rm -v `"${PWD}:${PWD}`" -w ${PWD} alpine/openssl rsa -pubout -in $PrivateKeyPath -out $PublicKeyPath"
Write-Host ""
exit 1
