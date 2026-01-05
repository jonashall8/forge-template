# Project Forge - Setup Script for Windows
# This script sets up the local development environment

Write-Host "🔥 Project Forge - Setup Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# Check prerequisites
Write-Host "`nChecking prerequisites..." -ForegroundColor Yellow

function Test-Command {
    param([string]$Command)
    if (Get-Command $Command -ErrorAction SilentlyContinue) {
        Write-Host "✓ $Command is installed" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ $Command is not installed" -ForegroundColor Red
        return $false
    }
}

$missing = $false
if (-not (Test-Command "docker")) { $missing = $true }
if (-not (Test-Command "docker-compose")) { $missing = $true }
Test-Command "java" | Out-Null
Test-Command "mvn" | Out-Null
Test-Command "terraform" | Out-Null
Test-Command "kubectl" | Out-Null
Test-Command "aws" | Out-Null

if ($missing) {
    Write-Host "`nError: Required tools are missing. Please install them first." -ForegroundColor Red
    exit 1
}

# Build Java SDK
Write-Host "`nBuilding Java Observability SDK..." -ForegroundColor Yellow
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Push-Location java-observability-sdk
    mvn clean install -DskipTests -q
    Pop-Location
    Write-Host "✓ SDK built successfully" -ForegroundColor Green
} else {
    Write-Host "⚠ Skipping SDK build (Maven not found)" -ForegroundColor Yellow
}

# Build Sample Microservice
Write-Host "`nBuilding Sample Microservice..." -ForegroundColor Yellow
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Push-Location sample-microservice
    mvn clean package -DskipTests -q
    Pop-Location
    Write-Host "✓ Microservice built successfully" -ForegroundColor Green
} else {
    Write-Host "⚠ Skipping microservice build (Maven not found)" -ForegroundColor Yellow
}

# Create Docker network if not exists
Write-Host "`nSetting up Docker network..." -ForegroundColor Yellow
docker network create project-forge 2>$null
Write-Host "✓ Docker network ready" -ForegroundColor Green

# Start services
Write-Host "`nStarting observability stack..." -ForegroundColor Yellow
Push-Location docker
docker-compose up -d
Pop-Location

Write-Host "`n✓ Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Services available at:"
Write-Host "  - Sample Microservice: http://localhost:8080"
Write-Host "  - Prometheus:          http://localhost:9090"
Write-Host "  - Grafana:             http://localhost:3000 (admin/admin)"
Write-Host "  - Kibana:              http://localhost:5601"
Write-Host "  - Elasticsearch:       http://localhost:9200"
Write-Host ""
Write-Host "Useful endpoints:"
Write-Host "  - Health:    http://localhost:8080/actuator/health"
Write-Host "  - Metrics:   http://localhost:8080/actuator/prometheus"
Write-Host "  - API:       http://localhost:8080/api/orders"
Write-Host ""
Write-Host "To view logs: docker-compose -f docker/docker-compose.yml logs -f"
Write-Host "To stop:      docker-compose -f docker/docker-compose.yml down"

