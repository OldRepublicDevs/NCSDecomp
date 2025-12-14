# Test that ncsdis.exe is properly recognized by NCSDecomp

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
$ToolsDir = Join-Path $RepoRoot "tools"
$NcsdisPath = Join-Path $ToolsDir "ncsdis.exe"

Write-Host "Testing ncsdis.exe recognition" -ForegroundColor Cyan
Write-Host "ncsdis.exe path: $NcsdisPath" -ForegroundColor Yellow

# Calculate SHA256
$sha256 = (Get-FileHash $NcsdisPath -Algorithm SHA256).Hash
Write-Host "Calculated SHA256: $sha256" -ForegroundColor Green

# Check against what we put in KnownExternalCompilers.java
$expectedSha256 = "B1F398C2F64F4ACF2F39C417E7C7EB6F5483369BB95853C63A009F925A2E257C"
if ($sha256 -eq $expectedSha256) {
    Write-Host "✓ SHA256 matches KnownExternalCompilers.NCSDIS!" -ForegroundColor Green
} else {
    Write-Host "✗ SHA256 mismatch!" -ForegroundColor Red
    Write-Host "  Expected: $expectedSha256" -ForegroundColor Yellow
    Write-Host "  Got:      $sha256" -ForegroundColor Yellow
    exit 1
}

Write-Host "`nAll checks passed!" -ForegroundColor Cyan
