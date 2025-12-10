# Copyright 2021-2025 NCSDecomp
# Licensed under the Business Source License 1.1 (BSL 1.1).
# Visit https://bolabaden.org for more information and other ventures
# See LICENSE.txt file in the project root for full license information.

# PowerShell script to run tests with 2-minute timeout and profiling
# Cross-platform compatible (Windows, macOS, Linux)
param(
    [int]$TimeoutSeconds = 120
)

# Detect platform (PowerShell Core 6+ has automatic variables, fallback for older versions)
if ($PSVersionTable.PSVersion.Major -ge 6) {
    # Use automatic variables in PowerShell Core 6+
    # $IsWindows is automatically available
} else {
    # Fallback for Windows PowerShell 5.1
    if (-not (Test-Path variable:IsWindows)) { $script:IsWindows = $env:OS -eq "Windows_NT" }
}

$junitStandalone = Join-Path "." (Join-Path "lib" "junit-platform-console-standalone-1.10.0.jar")
if (-not (Test-Path $junitStandalone)) {
    Write-Host "Error: JUnit JAR not found at $junitStandalone" -ForegroundColor Red
    exit 1
}

# Ensure build directory exists and has compiled classes
$buildDir = Join-Path "." "build"
if (-not (Test-Path $buildDir)) {
    Write-Host "Error: Build directory not found. Run build.ps1 first." -ForegroundColor Red
    exit 1
}

# Find test file in Maven test directory
$testFile = Join-Path "." (Join-Path "src" (Join-Path "test" (Join-Path "java" (Join-Path "com" (Join-Path "kotor" (Join-Path "resource" (Join-Path "formats" (Join-Path "ncs" "NCSDecompCLIRoundTripTest.java"))))))))
if (-not (Test-Path $testFile)) {
    # Try alternative location
    $testFile = Join-Path "." (Join-Path "com" (Join-Path "kotor" (Join-Path "resource" (Join-Path "formats" (Join-Path "ncs" "NCSDecompCLIRoundTripTest.java")))))
    if (-not (Test-Path $testFile)) {
        Write-Host "Error: Test file not found" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Compiling test..."
$testSourceDir = Join-Path "." (Join-Path "src" (Join-Path "test" "java"))
$mainSourceDir = Join-Path "." (Join-Path "src" (Join-Path "main" "java"))
$pathSeparator = if ($IsWindows) { ";" } else { ":" }
$sourcepath = "$testSourceDir$pathSeparator$mainSourceDir"
$cp = "$buildDir$pathSeparator$junitStandalone$pathSeparator."

$compileOutput = javac -cp $cp -d $buildDir -encoding UTF-8 -sourcepath $sourcepath $testFile 2>&1
$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    Write-Host ""
    if ($compileOutput) {
        $compileOutput | Write-Host
    }
    exit 1
}

$profileFile = Join-Path "test-work" "test_profile.txt"
Write-Host "Running tests with $TimeoutSeconds second timeout and profiling..."
Write-Host "Profiling output will be in: $profileFile"
Write-Host ""

# Start the Java process with profiling
$job = Start-Job -ScriptBlock {
    param($cp, $profilePath)
    $env:JAVA_TOOL_OPTIONS = "-XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -XX:LogFile=$profilePath -XX:+PrintCompilation"
    java -cp $cp -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -XX:LogFile=$profilePath com.kotor.resource.formats.ncs.NCSDecompCLIRoundTripTest 2>&1
} -ArgumentList $cp, $profileFile

# Wait for job with timeout
$result = Wait-Job -Job $job -Timeout $TimeoutSeconds

if ($null -eq $result) {
    Write-Host "`nTIMEOUT: Tests exceeded $TimeoutSeconds seconds. Killing process..." -ForegroundColor Red
    Stop-Job -Job $job
    Remove-Job -Job $job -Force
    exit 124
}

$output = Receive-Job -Job $job
Remove-Job -Job $job

# Output the results
$output | Write-Host

# Check exit code
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) {
    exit $exitCode
}

# Analyze profile if it exists
if (Test-Path $profileFile) {
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "PROFILE ANALYSIS" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

    # Parse compilation log to find hot methods
    $profileContent = Get-Content $profileFile -ErrorAction SilentlyContinue
    if ($profileContent) {
        # Extract method compilation info
        $hotMethods = $profileContent | Where-Object { $_ -match '^\s*\d+\s+\d+\s+[!%]' } |
            ForEach-Object {
                if ($_ -match '^\s*(\d+)\s+(\d+)\s+([!%]?)\s+(\d+)\s+([^\s]+)\s+(.+)') {
                    [PSCustomObject]@{
                        CompileID = [int]$matches[1]
                        Level = [int]$matches[2]
                        Special = $matches[3]
                        Size = [int]$matches[4]
                        Method = $matches[6]
                        Line = $_
                    }
                }
            } |
            Sort-Object -Property Size -Descending |
            Select-Object -First 20

        if ($hotMethods) {
            Write-Host "Top 20 methods by compiled size (potential bottlenecks):" -ForegroundColor Yellow
            Write-Host ""
            $hotMethods | ForEach-Object {
                $indicator = if ($_.Special -eq '!') { '[OSR]' } elseif ($_.Special -eq '%') { '[ON-STACK]' } else { '' }
                Write-Host ("  {0,6} bytes {1,-8} {2}" -f $_.Size, $indicator, $_.Method)
            }
        }

        # Count total compilations
        $totalCompilations = ($profileContent | Where-Object { $_ -match '^\s*\d+\s+\d+\s+\d+' }).Count
        Write-Host ""
        Write-Host "Total method compilations: $totalCompilations"
    } else {
        Write-Host "Profile file is empty or could not be read." -ForegroundColor Yellow
    }

    Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Full profile log: $profileFile" -ForegroundColor Gray
}


