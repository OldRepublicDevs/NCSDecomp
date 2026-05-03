# Compare pcode output formats between ncsdis.exe and nwnnsscomp_kscript.exe
# This script tests both tools on the same NCS file to identify formatting differences

param(
    [string]$NcsFile = "test-work\tmp2\a_sisteratk.ncs"
)

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
$ToolsDir = Join-Path $RepoRoot "tools"
$NcsdisPath = Join-Path $ToolsDir "ncsdis.exe"
$NwnnsscompPath = Join-Path $ToolsDir "nwnnsscomp_kscript.exe"

# Resolve NCS file path
$NcsPath = Join-Path $RepoRoot $NcsFile
if (-not (Test-Path $NcsPath)) {
    Write-Error "NCS file not found: $NcsPath"
    exit 1
}

Write-Host "Testing pcode output formats" -ForegroundColor Cyan
Write-Host "NCS File: $NcsPath" -ForegroundColor Yellow
Write-Host ""

# Create temp directory for output
$TempDir = Join-Path $RepoRoot "test-work\pcode-compare-temp"
if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempDir | Out-Null

$NcsdisOutput = Join-Path $TempDir "ncsdis.pcode"
$NwnnsscompOutput = Join-Path $TempDir "nwnnsscomp.pcode"

# Test 1: ncsdis.exe
Write-Host "Running ncsdis.exe..." -ForegroundColor Green
if (Test-Path $NcsdisPath) {
    # ncsdis.exe usage: ncsdis.exe <input.ncs> <output.pcode>
    & $NcsdisPath $NcsPath $NcsdisOutput 2>&1 | Out-Host
    if (Test-Path $NcsdisOutput) {
        $ncsdisContent = Get-Content $NcsdisOutput -Raw
        Write-Host "ncsdis.exe output length: $($ncsdisContent.Length) bytes" -ForegroundColor Gray
        Write-Host "First 500 chars:" -ForegroundColor Gray
        Write-Host $ncsdisContent.Substring(0, [Math]::Min(500, $ncsdisContent.Length))
        Write-Host ""
    } else {
        Write-Host "ERROR: ncsdis.exe did not produce output file" -ForegroundColor Red
    }
} else {
    Write-Host "ERROR: ncsdis.exe not found at: $NcsdisPath" -ForegroundColor Red
}

# Test 2: nwnnsscomp_kscript.exe
Write-Host "Running nwnnsscomp_kscript.exe..." -ForegroundColor Green
if (Test-Path $NwnnsscompPath) {
    # nwnnsscomp usage: nwnnsscomp_kscript.exe -d <input.ncs> -o <output.pcode> -g 1
    & $NwnnsscompPath -d $NcsPath -o $NwnnsscompOutput -g 1 2>&1 | Out-Host
    if (Test-Path $NwnnsscompOutput) {
        $nwnnsscompContent = Get-Content $NwnnsscompOutput -Raw
        Write-Host "nwnnsscomp_kscript.exe output length: $($nwnnsscompContent.Length) bytes" -ForegroundColor Gray
        Write-Host "First 500 chars:" -ForegroundColor Gray
        Write-Host $nwnnsscompContent.Substring(0, [Math]::Min(500, $nwnnsscompContent.Length))
        Write-Host ""
    } else {
        Write-Host "ERROR: nwnnsscomp_kscript.exe did not produce output file" -ForegroundColor Red
    }
} else {
    Write-Host "ERROR: nwnnsscomp_kscript.exe not found at: $NwnnsscompPath" -ForegroundColor Red
}

# Compare outputs
Write-Host "=== COMPARISON ===" -ForegroundColor Cyan
if ((Test-Path $NcsdisOutput) -and (Test-Path $NwnnsscompOutput)) {
    $ncsdisLines = Get-Content $NcsdisOutput
    $nwnnsscompLines = Get-Content $NwnnsscompOutput

    Write-Host "ncsdis.exe lines: $($ncsdisLines.Count)" -ForegroundColor Gray
    Write-Host "nwnnsscomp_kscript.exe lines: $($nwnnsscompLines.Count)" -ForegroundColor Gray
    Write-Host ""

    # Show first 20 lines side by side
    Write-Host "First 20 lines comparison:" -ForegroundColor Yellow
    Write-Host ""
    for ($i = 0; $i -lt [Math]::Min(20, [Math]::Max($ncsdisLines.Count, $nwnnsscompLines.Count)); $i++) {
        $ncsdisLine = if ($i -lt $ncsdisLines.Count) { $ncsdisLines[$i] } else { "" }
        $nwnnsscompLine = if ($i -lt $nwnnsscompLines.Count) { $nwnnsscompLines[$i] } else { "" }

        if ($ncsdisLine -eq $nwnnsscompLine) {
            Write-Host "  [$i] MATCH: $ncsdisLine" -ForegroundColor Green
        } else {
            Write-Host "  [$i] DIFF:" -ForegroundColor Red
            Write-Host "    ncsdis:     $ncsdisLine" -ForegroundColor Magenta
            Write-Host "    nwnnsscomp: $nwnnsscompLine" -ForegroundColor Cyan
        }
    }

    # Identify specific formatting differences
    Write-Host ""
    Write-Host "=== PATTERN ANALYSIS ===" -ForegroundColor Cyan

    # Check for common patterns
    $ncsdisHasColons = ($ncsdisLines -match ":").Count
    $nwnnsscompHasColons = ($nwnnsscompLines -match ":").Count
    Write-Host "Lines with colons - ncsdis: $ncsdisHasColons, nwnnsscomp: $nwnnsscompHasColons"

    # Check for hex formatting
    $ncsdisHasHex = ($ncsdisLines -match "0x[0-9A-Fa-f]+").Count
    $nwnnsscompHasHex = ($nwnnsscompLines -match "0x[0-9A-Fa-f]+").Count
    Write-Host "Lines with hex (0x...) - ncsdis: $ncsdisHasHex, nwnnsscomp: $nwnnsscompHasHex"

    # Check for spacing patterns
    $ncsdisMultiSpaces = ($ncsdisLines -match "\s{2,}").Count
    $nwnnsscompMultiSpaces = ($nwnnsscompLines -match "\s{2,}").Count
    Write-Host "Lines with multiple spaces - ncsdis: $ncsdisMultiSpaces, nwnnsscomp: $nwnnsscompMultiSpaces"

    Write-Host ""
    Write-Host "Output files saved to:" -ForegroundColor Yellow
    Write-Host "  ncsdis:     $NcsdisOutput" -ForegroundColor Gray
    Write-Host "  nwnnsscomp: $NwnnsscompOutput" -ForegroundColor Gray
} else {
    Write-Host "Cannot compare - one or both output files missing" -ForegroundColor Red
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
