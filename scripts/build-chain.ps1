# Copyright 2021-2025 NCSDecomp
# Licensed under the Business Source License 1.1 (BSL 1.1).
# Visit https://bolabaden.org for more information and other ventures
# See LICENSE.txt file in the project root for full license information.

# Build chain script that runs build, publish, and test commands
# Stops on any error to prevent continuing with broken builds

$ErrorActionPreference = "Stop"

Write-Host "NCSDecomp Build Chain" -ForegroundColor Green
Write-Host "====================" -ForegroundColor Green
Write-Host ""

# Step 1: Build
Write-Host "Step 1: Building JAR files..." -ForegroundColor Yellow
& "$PSScriptRoot\build.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Build completed successfully" -ForegroundColor Green
Write-Host ""

# Step 2: Publish
Write-Host "Step 2: Publishing distribution package..." -ForegroundColor Yellow
& "$PSScriptRoot\publish.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Publish failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Publish completed successfully" -ForegroundColor Green
Write-Host ""

# Step 3: Test CLI JAR
Write-Host "Step 3: Testing CLI JAR..." -ForegroundColor Yellow
$cliJar = Join-Path "." (Join-Path "target" (Join-Path "assembly" "NCSDecompCLI.jar"))
if (-not (Test-Path $cliJar)) {
    Write-Host "Error: CLI JAR not found at $cliJar" -ForegroundColor Red
    exit 1
}
java -jar $cliJar --version
if ($LASTEXITCODE -ne 0) {
    Write-Host "CLI JAR test failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "CLI JAR test completed successfully" -ForegroundColor Green
Write-Host ""

# Step 4: Test GUI JAR
Write-Host "Step 4: Testing GUI JAR..." -ForegroundColor Yellow
$guiJar = Join-Path "." (Join-Path "target" (Join-Path "assembly" "NCSDecomp.jar"))
if (-not (Test-Path $guiJar)) {
    Write-Host "Warning: GUI JAR not found at $guiJar, skipping test" -ForegroundColor Yellow
} else {
    # GUI JAR requires a headless test or just verify it exists
    Write-Host "GUI JAR found and ready" -ForegroundColor Green
}
Write-Host ""

Write-Host "================================" -ForegroundColor Green
Write-Host "Build chain completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Output files:" -ForegroundColor Cyan
Write-Host "  CLI: $cliJar" -ForegroundColor White
if (Test-Path $guiJar) {
    Write-Host "  GUI: $guiJar" -ForegroundColor White
}
Write-Host ""

exit 0

