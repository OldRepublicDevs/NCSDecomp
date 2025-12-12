#!/usr/bin/env pwsh
# Simple script to test a single NSS file
# Usage: ./scripts/test_single.ps1 <filename> [game]
# Example: ./scripts/test_single.ps1 k_def_buff.nss k1

param(
    [Parameter(Mandatory=$true)]
    [string]$Filename,

    [Parameter(Mandatory=$false)]
    [string]$Game = "k1"
)

$ErrorActionPreference = "Stop"

# Compile test classes
Write-Host "Compiling..." -ForegroundColor Cyan
mvn test-compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

# Run single file test
Write-Host "Testing: $Filename (game=$Game)" -ForegroundColor Cyan
Write-Host ""

java -cp "target/test-classes;target/classes;lib/*" `
    com.kotor.resource.formats.ncs.NCSDecompCLIRoundTripTest `
    $Filename $Game

exit $LASTEXITCODE

