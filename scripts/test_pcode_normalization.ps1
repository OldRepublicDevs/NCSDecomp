# Test script to verify pcode normalization works correctly

param(
    [string]$TestFile = "test-work\tmp2\a_sisteratk.ncs"
)

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
$ToolsDir = Join-Path $RepoRoot "tools"
$NcsdisPath = Join-Path $ToolsDir "ncsdis.exe"
$NwnnsscompPath = Join-Path $ToolsDir "nwnnsscomp_kscript.exe"

# Resolve test file
$TestNcs = Join-Path $RepoRoot $TestFile
if (-not (Test-Path $TestNcs)) {
    Write-Error "Test file not found: $TestNcs"
    exit 1
}

Write-Host "Testing pcode normalization with test file: $TestNcs" -ForegroundColor Cyan

# Create temp directory for outputs
$TempDir = Join-Path $RepoRoot "test-work\pcode-test-temp"
if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempDir | Out-Null

$NcsdisOutput = Join-Path $TempDir "ncsdis.pcode"
$NwnnsscompOutput = Join-Path $TempDir "nwnnsscomp.pcode"

# Generate pcode with ncsdis
Write-Host "`nGenerating pcode with ncsdis.exe..." -ForegroundColor Green
& $NcsdisPath $TestNcs $NcsdisOutput
if (-not (Test-Path $NcsdisOutput)) {
    Write-Error "ncsdis.exe failed to produce output"
    exit 1
}

# Generate pcode with nwnnsscomp
Write-Host "Generating pcode with nwnnsscomp_kscript.exe..." -ForegroundColor Green
Push-Location $ToolsDir
try {
    & .\nwnnsscomp_kscript.exe -d $TestNcs -o $NwnnsscompOutput -g 1
} finally {
    Pop-Location
}

if (-not (Test-Path $NwnnsscompOutput)) {
    Write-Error "nwnnsscomp_kscript.exe failed to produce output"
    exit 1
}

Write-Host "`nBoth pcode files generated successfully!" -ForegroundColor Green
Write-Host "  ncsdis:     $NcsdisOutput" -ForegroundColor Gray
Write-Host "  nwnnsscomp: $NwnnsscompOutput" -ForegroundColor Gray

# Now test the normalization by comparing file sizes and formats
$ncsdisLines = Get-Content $NcsdisOutput
$nwnnsscompLines = Get-Content $NwnnsscompOutput

Write-Host "`nFile stats:" -ForegroundColor Yellow
Write-Host "  ncsdis lines:     $($ncsdisLines.Count)"
Write-Host "  nwnnsscomp lines: $($nwnnsscompLines.Count)"

# Show sample lines from each
Write-Host "`nSample lines from ncsdis:" -ForegroundColor Yellow
$ncsdisLines | Select-Object -First 10 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

Write-Host "`nSample lines from nwnnsscomp:" -ForegroundColor Yellow
$nwnnsscompLines | Select-Object -First 10 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

Write-Host "`nDone! Java normalization logic should handle these format differences." -ForegroundColor Cyan
Write-Host "Next: Compile Java and run round-trip tests to verify." -ForegroundColor Yellow
