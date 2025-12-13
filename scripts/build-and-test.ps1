# Copyright 2021-2025 NCSDecomp
# Licensed under the Business Source License 1.1 (BSL 1.1).
# Visit https://bolabaden.org for more information and other ventures
# See LICENSE.txt file in the project root for full license information.

# Build, publish, and test chain - matches user's exact command format
# Stops on any error to prevent continuing with broken builds

$ErrorActionPreference = "Stop"

Write-Host "NCSDecomp Build and Test Chain" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green
Write-Host ""

# Step 1: Build
Write-Host "[1/4] Building..." -ForegroundColor Yellow
& "$PSScriptRoot\build.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Build: SUCCESS" -ForegroundColor Green
Write-Host ""

# Step 2: Publish
Write-Host "[2/4] Publishing..." -ForegroundColor Yellow
& "$PSScriptRoot\publish.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Publish failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Publish: SUCCESS" -ForegroundColor Green
Write-Host ""

# Step 3: Test CLI JAR
Write-Host "[3/4] Testing CLI JAR..." -ForegroundColor Yellow
$cliJar = Join-Path "." (Join-Path "target" (Join-Path "assembly" "NCSDecompCLI.jar"))
if (-not (Test-Path $cliJar)) {
    Write-Host "ERROR: CLI JAR not found at $cliJar" -ForegroundColor Red
    exit 1
}
java -jar $cliJar --version
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: CLI JAR test failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "CLI JAR test: SUCCESS" -ForegroundColor Green
Write-Host ""

# Step 4: Test GUI JAR (just verify it exists and can be read)
Write-Host "[4/4] Verifying GUI JAR..." -ForegroundColor Yellow
$guiJar = Join-Path "." (Join-Path "target" (Join-Path "assembly" "NCSDecomp.jar"))
if (-not (Test-Path $guiJar)) {
    Write-Host "ERROR: GUI JAR not found at $guiJar" -ForegroundColor Red
    exit 1
}
# GUI JAR requires GUI environment, so just verify it's readable
try {
    $null = [System.IO.File]::OpenRead($guiJar).Close()
    Write-Host "GUI JAR: SUCCESS (verified readable)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: GUI JAR exists but cannot be read: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "================================" -ForegroundColor Green
Write-Host "All steps completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Output files:" -ForegroundColor Cyan
Write-Host "  CLI: $cliJar" -ForegroundColor White
Write-Host "  GUI: $guiJar" -ForegroundColor White
Write-Host ""

exit 0

