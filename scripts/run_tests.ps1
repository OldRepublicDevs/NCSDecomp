# Copyright 2021-2025 NCSDecomp
# Licensed under the Business Source License 1.1 (BSL 1.1).
# Visit https://bolabaden.org for more information and other ventures
# See LICENSE.txt file in the project root for full license information.

# PowerShell script to run tests with profiling
# Cross-platform compatible (Windows, macOS, Linux)
# This version outputs raw stdout/stderr directly to console without any filtering/redirection
#
# Usage examples:
#   .\scripts\run_tests.ps1                                    # Default: job-based execution with streaming output
#   .\scripts\run_tests.ps1 -DirectExecution -TeeOutput         # Direct execution with Tee-Object logging
#   .\scripts\run_tests.ps1 -DirectExecution -TeeOutput -LogFile "custom.log"
#   .\scripts\run_tests.ps1 -NoResume -TimeoutSeconds 600      # Start from beginning with 600s timeout
#   .\scripts\run_tests.ps1 -BuildDir "target\classes" -JUnitJar "lib\junit.jar"  # Custom paths
#   .\scripts\run_tests.ps1 -ClassPath "target\classes;lib\junit.jar;."  # Fully custom classpath
#   .\scripts\run_tests.ps1 -NoProfiling                      # Disable profiling
param(
    [switch]$NoResume = $false,
    [int]$TimeoutSeconds = 0,
    [string]$LogFile = "test_output.log",
    [switch]$SuppressStderr = $false,
    [switch]$BatchOutput = $false,
    [switch]$ShowCompilationErrors = $false,
    [switch]$DirectExecution = $false,
    [switch]$TeeOutput = $false,
    [switch]$NoProfiling = $false,
    [string]$BuildDir = "",
    [string]$JUnitJar = "",
    [string]$ClassPath = ""
)

# Set up file logging if LogFile parameter is provided
$logFileStream = $null
if ($LogFile) {
    try {
        $logFileStream = [System.IO.StreamWriter]::new($LogFile, $false, [System.Text.Encoding]::UTF8)
        Write-Host "Logging output to: $LogFile" -ForegroundColor Gray
    } catch {
        Write-Host "Warning: Could not open log file '$LogFile': $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "Continuing without file logging..." -ForegroundColor Yellow
    }
}

# Helper function to write to both console and log file
function Write-ToConsoleAndLog {
    param(
        [string]$Message,
        [switch]$NoNewline,
        [ConsoleColor]$ForegroundColor
    )
    if ($NoNewline) {
        if ($ForegroundColor) {
            Write-Host $Message -NoNewline -ForegroundColor $ForegroundColor
        } else {
            Write-Host $Message -NoNewline
        }
        if ($logFileStream) { $logFileStream.Write($Message) }
    } else {
        if ($ForegroundColor) {
            Write-Host $Message -ForegroundColor $ForegroundColor
        } else {
            Write-Host $Message
        }
        if ($logFileStream) { $logFileStream.WriteLine($Message) }
    }
}

# Helper function to write raw output (for job output) to both console and log file
function Write-RawToConsoleAndLog {
    param([string]$Message)
    [Console]::WriteLine($Message)
    if ($logFileStream) { $logFileStream.WriteLine($Message) }
}

# Cleanup function to close log file
function Close-LogFile {
    if ($logFileStream) {
        try {
            $logFileStream.Close()
            Write-Host "Log file saved: $LogFile" -ForegroundColor Gray
        } catch {
            Write-Host "Warning: Error closing log file: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

# Detect platform (PowerShell Core 6+ has automatic variables, fallback for older versions)
if ($PSVersionTable.PSVersion.Major -ge 6) {
    # Use automatic variables in PowerShell Core 6+
    # $IsWindows is automatically available
} else {
    # Fallback for Windows PowerShell 5.1
    if (-not (Test-Path variable:IsWindows)) { $script:IsWindows = $env:OS -eq "Windows_NT" }
}

# Ensure lib directory exists
$libDir = Join-Path "." "lib"
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir -Force | Out-Null
}

# Try to find JUnit JAR (prefer exact version, fall back to any version)
if (-not [string]::IsNullOrEmpty($JUnitJar)) {
    $junitStandalone = $JUnitJar
    if (-not [System.IO.Path]::IsPathRooted($junitStandalone)) {
        $junitStandalone = Join-Path "." $junitStandalone
    }
    if (-not (Test-Path $junitStandalone)) {
        Write-Host "Error: Specified JUnit JAR not found: $junitStandalone" -ForegroundColor Red
        exit 1
    }
    Write-Host "Using specified JUnit JAR: $junitStandalone" -ForegroundColor Gray
} else {
    $junitVersion = "1.10.0"
    $junitJarName = "junit-platform-console-standalone-$junitVersion.jar"
    $junitStandalone = Join-Path $libDir $junitJarName

    if (-not (Test-Path $junitStandalone)) {
        # Try to find any version of junit-platform-console-standalone in lib/
        $existingJars = Get-ChildItem -Path $libDir -Filter "junit-platform-console-standalone-*.jar" -ErrorAction SilentlyContinue
        if ($existingJars) {
            # Use the latest version found (sort by name, which should sort versions correctly)
            $latestJar = $existingJars | Sort-Object Name -Descending | Select-Object -First 1
            $junitStandalone = $latestJar.FullName
            Write-Host "Found existing JUnit JAR: $($latestJar.Name)" -ForegroundColor Green
        } else {
            # Download from Maven Central
            Write-Host "JUnit JAR not found. Downloading from Maven Central..." -ForegroundColor Yellow
            $mavenUrl = "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$junitVersion/$junitJarName"

            try {
                Write-Host "Downloading from: $mavenUrl" -ForegroundColor Gray
                $ProgressPreference = 'SilentlyContinue'
                Invoke-WebRequest -Uri $mavenUrl -OutFile $junitStandalone -UseBasicParsing
                $ProgressPreference = 'Continue'

                if (Test-Path $junitStandalone) {
                    $fileSize = (Get-Item $junitStandalone).Length / 1MB
                    Write-Host "Successfully downloaded JUnit JAR ($([math]::Round($fileSize, 2)) MB)" -ForegroundColor Green
                } else {
                    Write-Host "Error: Download failed - file not found after download" -ForegroundColor Red
                    exit 1
                }
            } catch {
                Write-Host "Error: Failed to download JUnit JAR from Maven Central" -ForegroundColor Red
                Write-Host "  URL: $mavenUrl" -ForegroundColor Gray
                Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
                exit 1
            }
        }
    }
}

# Ensure build directory exists and has compiled classes (Java/Maven idiomatic: target/classes)
if ([string]::IsNullOrEmpty($BuildDir)) {
    $buildDir = Join-Path "." (Join-Path "target" "classes")
} else {
    $buildDir = $BuildDir
    if (-not [System.IO.Path]::IsPathRooted($buildDir)) {
        $buildDir = Join-Path "." $buildDir
    }
}
if (-not (Test-Path $buildDir)) {
    Write-Host "Error: Build directory not found: $buildDir. Run build.ps1 first." -ForegroundColor Red
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
# Clear JAVA_TOOL_OPTIONS before compilation to avoid affecting javac
$env:JAVA_TOOL_OPTIONS = ""

$testSourceDir = Join-Path "." (Join-Path "src" (Join-Path "test" "java"))
$mainSourceDir = Join-Path "." (Join-Path "src" (Join-Path "main" "java"))
$pathSeparator = if ($IsWindows) { ";" } else { ":" }
$sourcepath = "$testSourceDir$pathSeparator$mainSourceDir"

# Build classpath - use custom if provided, otherwise construct from buildDir and JUnit JAR
if (-not [string]::IsNullOrEmpty($ClassPath)) {
    $cp = $ClassPath
    Write-Host "Using custom classpath: $cp" -ForegroundColor Gray
} else {
    $cp = "$buildDir$pathSeparator$junitStandalone$pathSeparator."
}

# Detect Java version and use appropriate flags for Java 8 compatibility
# --release 8 is the modern way (Java 9+) and automatically handles bootstrap class path
# Fall back to -source 8 -target 8 -Xlint:-options for Java 8
$javaVersionOutput = javac -version 2>&1 | Out-String
$javaMajorVersion = 0
$versionDetected = $false

# Try to match javac version output (handles both "javac 11" and "javac 1.8" formats)
if ($javaVersionOutput -match 'javac\s+(?:1\.)?(\d+)') {
    $javaMajorVersion = [int]$matches[1]
    $versionDetected = $true
}

# If javac version detection failed, try java -version as fallback
if (-not $versionDetected) {
    $javaVersionOutput2 = java -version 2>&1 | Out-String
    if ($javaVersionOutput2 -match 'version\s+"(?:1\.)?(\d+)') {
        $javaMajorVersion = [int]$matches[1]
        $versionDetected = $true
    } elseif ($javaVersionOutput2 -match 'version\s+(?:1\.)?(\d+)') {
        $javaMajorVersion = [int]$matches[1]
        $versionDetected = $true
    }
}

# Default to Java 8 behavior if version cannot be detected
if (-not $versionDetected) {
    Write-Host "Warning: Could not detect Java version, defaulting to Java 8 compatibility flags" -ForegroundColor Yellow
    $javaMajorVersion = 8
}

$compileOutput = $null
if ($javaMajorVersion -ge 9) {
    # Use --release 8 (best practice for Java 9+)
    if ($ShowCompilationErrors) {
        $compileOutput = javac -cp $cp -d $buildDir -encoding UTF-8 --release 8 -sourcepath $sourcepath $testFile 2>&1
    } else {
        javac -cp $cp -d $buildDir -encoding UTF-8 --release 8 -sourcepath $sourcepath $testFile
    }
} else {
    # Java 8: use -source 8 -target 8 with -Xlint:-options to suppress warnings
    if ($ShowCompilationErrors) {
        $compileOutput = javac -cp $cp -d $buildDir -encoding UTF-8 -source 8 -target 8 -Xlint:-options -sourcepath $sourcepath $testFile 2>&1
    } else {
        javac -cp $cp -d $buildDir -encoding UTF-8 -source 8 -target 8 -Xlint:-options -sourcepath $sourcepath $testFile
    }
}
$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    Write-Host ""
    if ($ShowCompilationErrors -and $compileOutput) {
        $compileOutput | Write-Host
    }
    Close-LogFile
    exit 1
}

# Ensure test-work directory exists
$testWorkDir = "test-work"
if (-not (Test-Path $testWorkDir)) {
    New-Item -ItemType Directory -Path $testWorkDir -Force | Out-Null
}

$profileTextFile = Join-Path $testWorkDir "test_profile.txt"

# Check if profiling should be enabled
$hprofAvailable = $false
if (-not $NoProfiling) {
    if ($TimeoutSeconds -gt 0) {
        Write-Host "Running tests with $TimeoutSeconds second timeout and profiling..." -ForegroundColor Cyan
    } else {
        Write-Host "Running tests with profiling..." -ForegroundColor Cyan
    }
    Write-Host "Profiling output will be in: $profileTextFile" -ForegroundColor Gray
    Write-Host ""

    # Try to use hprof for profiling (works on most Java versions, provides execution time data)
    # cpu=times provides method-level timing information
    # If hprof is not available, profiling will be skipped
    # Note: Don't set JAVA_TOOL_OPTIONS here - it affects javac too. Set it only in the job scriptblock.
    # hprof is part of the JDK, so if it's not available, it's likely a JDK installation issue or hprof was removed (deprecated in Java 9+)
    $hprofTestOutput = java -agentlib:hprof=help 2>&1
    $hprofTestString = $hprofTestOutput | Out-String
    # Check if hprof is available - it should not contain error messages
    if ($LASTEXITCODE -eq 0 -and $hprofTestString -notmatch "Could not find agent library|Can't find dependent libraries|Error occurred during initialization|hprof is not available") {
        $hprofAvailable = $true
        Write-Host "Using hprof profiling" -ForegroundColor Gray
    } else {
        $hprofAvailable = $false
        Write-Host "Note: hprof profiling not available (hprof library not found in JDK), running tests without profiling" -ForegroundColor Yellow
    }
} else {
    if ($TimeoutSeconds -gt 0) {
        Write-Host "Running tests with $TimeoutSeconds second timeout (profiling disabled)..." -ForegroundColor Cyan
    } else {
        Write-Host "Running tests (profiling disabled)..." -ForegroundColor Cyan
    }
    Write-Host ""
}

# Keep JAVA_TOOL_OPTIONS cleared (already cleared before compilation)
# It will be set in the job scriptblock only for the java runtime

# Build Java arguments
$javaArgs = @()
if ($NoResume) {
    $javaArgs = @("--no-resume")
}

# Prepare JAVA_TOOL_OPTIONS for profiling
$originalJavaToolOptions = $env:JAVA_TOOL_OPTIONS
if ($hprofAvailable) {
    $env:JAVA_TOOL_OPTIONS = "-agentlib:hprof=cpu=times,file=$profileTextFile"
} else {
    $env:JAVA_TOOL_OPTIONS = ""
}

# Create a scriptblock that runs java and outputs directly (for job execution)
$scriptBlock = {
    param($cp, $profilePath, $noResume, $useHprof, $suppressStderr)
    if ($useHprof) {
        $env:JAVA_TOOL_OPTIONS = "-agentlib:hprof=cpu=times,file=$profilePath"
    } else {
        $env:JAVA_TOOL_OPTIONS = ""
    }
    $javaArgs = @()
    if ($noResume) {
        $javaArgs = @("--no-resume")
    }
    if ($suppressStderr) {
        & java -cp $cp com.kotor.resource.formats.ncs.NCSDecompCLIRoundTripTest @javaArgs 2>$null
    } else {
        & java -cp $cp com.kotor.resource.formats.ncs.NCSDecompCLIRoundTripTest @javaArgs
    }
}

# Choose execution mode: direct or job-based
if ($DirectExecution) {
    # Direct execution mode - run synchronously
    Write-Host "Running tests in direct execution mode..." -ForegroundColor Cyan
    if ($TeeOutput -and $LogFile) {
        # Close the existing log file stream if open, since Tee-Object will handle logging
        Close-LogFile
        $logFileStream = $null
    }

    $exitCode = 0
    try {
        # Build the java command
        $javaCmdArgs = @("-cp", $cp, "com.kotor.resource.formats.ncs.NCSDecompCLIRoundTripTest") + $javaArgs

        if ($TeeOutput -and $LogFile) {
            # Use Tee-Object to both display and log output simultaneously
            if ($SuppressStderr) {
                & java @javaCmdArgs 2>$null | Tee-Object -FilePath $LogFile
            } else {
                & java @javaCmdArgs 2>&1 | Tee-Object -FilePath $LogFile
            }
        } else {
            # Normal execution - output to console, log via stream writer if LogFile is set
            if ($SuppressStderr) {
                & java @javaCmdArgs 2>$null | ForEach-Object {
                    Write-Host $_
                    if ($logFileStream) { $logFileStream.WriteLine($_) }
                }
            } else {
                & java @javaCmdArgs 2>&1 | ForEach-Object {
                    Write-Host $_
                    if ($logFileStream) { $logFileStream.WriteLine($_) }
                }
            }
        }
        $exitCode = $LASTEXITCODE
    } finally {
        # Restore JAVA_TOOL_OPTIONS
        $env:JAVA_TOOL_OPTIONS = $originalJavaToolOptions
    }

    # Skip profiling analysis in direct execution mode (can be added later if needed)
    Close-LogFile

    if ($exitCode -ne 0) {
        exit $exitCode
    }
    exit 0
}

# Start job (only if not using direct execution)
$job = Start-Job -ScriptBlock $scriptBlock -ArgumentList $cp, $profileTextFile, $NoResume, $hprofAvailable, $SuppressStderr

if ($BatchOutput) {
    # Batch output mode: wait for job completion and output all at once (like run_tests_with_timeout.ps1)
    if ($TimeoutSeconds -gt 0) {
        $result = Wait-Job -Job $job -Timeout $TimeoutSeconds
        if ($null -eq $result) {
            Write-ToConsoleAndLog -Message "`nTIMEOUT: Tests exceeded $TimeoutSeconds seconds. Killing process..." -ForegroundColor Red
            Stop-Job -Job $job
            Remove-Job -Job $job -Force
            Close-LogFile
            exit 124
        }
    } else {
        $result = Wait-Job -Job $job
    }

    # Get all output at once
    $output = Receive-Job -Job $job
    if ($output) {
        foreach ($line in $output) {
            Write-RawToConsoleAndLog -Message $line
        }
    }
} else {
    # Real-time streaming mode (default): monitor job and receive output in real-time
    # Output is streamed directly to console as it becomes available
    $completed = $false
    $startTime = Get-Date
    $timeout = if ($TimeoutSeconds -gt 0) { New-TimeSpan -Seconds $TimeoutSeconds } else { $null }

    while (-not $completed) {
        # Check for timeout if specified
        if ($null -ne $timeout) {
            $elapsed = (Get-Date) - $startTime
            if ($elapsed -gt $timeout) {
                Write-ToConsoleAndLog -Message "`nTIMEOUT: Tests exceeded $TimeoutSeconds seconds. Killing process..." -ForegroundColor Red
                Stop-Job -Job $job
                Remove-Job -Job $job -Force
                Close-LogFile
                exit 124
            }
        }

        # Receive any available output and write directly to console (raw, no filtering)
        # Receive-Job captures both stdout and stderr from the job
        $output = Receive-Job -Job $job
        if ($output) {
            # Output directly to console - preserves both stdout and stderr
            foreach ($line in $output) {
                Write-RawToConsoleAndLog -Message $line
            }
        }

        # Check if job is complete
        if ($job.State -eq "Completed" -or $job.State -eq "Failed" -or $job.State -eq "Stopped") {
            $completed = $true
        } else {
            # Small delay to avoid busy-waiting
            Start-Sleep -Milliseconds 50
        }
    }

    # Get any remaining output that might have been buffered
    $remainingOutput = Receive-Job -Job $job
    if ($remainingOutput) {
        foreach ($line in $remainingOutput) {
            Write-RawToConsoleAndLog -Message $line
        }
    }
}

# Clean up job
Remove-Job -Job $job -Force

# Check exit code - for jobs, we check the state
# If the job failed or was stopped, exit with error code
if ($job.State -eq "Failed" -or $job.State -eq "Stopped") {
    Close-LogFile
    exit 1
}

# Analyze profile if it exists and hprof was used
if ($hprofAvailable) {
    if (Test-Path $profileTextFile) {
        Write-Host ""
        Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
        Write-Host "PROFILE ANALYSIS - Performance Bottlenecks (cProfile-style)" -ForegroundColor Cyan
        Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

        # Parse hprof output (works for all Java versions)
        $profileContent = Get-Content $profileTextFile -ErrorAction SilentlyContinue
        if ($profileContent) {
        $methodData = @{}
        $inCpuSection = $false

        foreach ($line in $profileContent) {
            # hprof format: CPU SAMPLES BEGIN / CPU TIME (ms) BEGIN
            if ($line -match 'CPU TIME \(ms\) BEGIN') {
                $inCpuSection = $true
                continue
            }
            if ($line -match 'CPU TIME \(ms\) END') {
                $inCpuSection = $false
                continue
            }

            if ($inCpuSection) {
                # Parse hprof CPU time format: rank self accum count trace method
                # Example: rank   self  accum   count trace method
                #          1  10.00%  10.00%       1 300001 com.example.Class.method()V
                # Match: number, percentage, percentage, number, number, method signature
                if ($line -match '^\s*(\d+)\s+(\d+\.?\d*)%\s+(\d+\.?\d*)%\s+(\d+)\s+(\d+)\s+(.+)') {
                    $selfPercent = [double]$matches[2]
                    $accumPercent = [double]$matches[3]
                    $count = [int]$matches[4]
                    $methodSig = $matches[6].Trim()

                    # Parse method signature: com.example.Class.method()V or com.example.Class.method(Ljava/lang/String;)V
                    # Extract class name (everything before last dot before method name)
                    if ($methodSig -match '^(.+)\.([^\.]+)\(.+\)') {
                        $fullClassName = $matches[1]
                        $methodName = $matches[2]

                        # Try to find source file location
                        $sourceFile = $null
                        $lineNumber = 0

                        # Convert package to path: com.kotor.resource -> src/main/java/com/kotor/resource
                        $classPath = $fullClassName.Replace('.', '/')
                        $possiblePaths = @(
                            "src/main/java/$classPath.java",
                            "src/test/java/$classPath.java"
                        )

                        foreach ($path in $possiblePaths) {
                            if (Test-Path $path) {
                                $sourceFile = $path
                                # Try to find method in file to get line number
                                $fileContent = Get-Content $path -ErrorAction SilentlyContinue
                                if ($fileContent) {
                                    for ($i = 0; $i -lt $fileContent.Length; $i++) {
                                        if ($fileContent[$i] -match "^\s*(public|private|protected)?\s*(static)?\s*\w+\s+$methodName\s*\(") {
                                            $lineNumber = $i + 1
                                            break
                                        }
                                    }
                                }
                                break
                            }
                        }

                        $key = $methodSig
                        if (-not $methodData.ContainsKey($key)) {
                            $methodData[$key] = @{
                                Method = $methodSig
                                ClassName = $fullClassName
                                MethodName = $methodName
                                SelfTime = 0.0
                                TotalTime = 0.0
                                Calls = 0
                                SourceFile = $sourceFile
                                LineNumber = $lineNumber
                            }
                        }

                        $methodData[$key].SelfTime += $selfPercent
                        $methodData[$key].TotalTime += $accumPercent
                        $methodData[$key].Calls += $count
                    }
                }
            }
        }

        if ($methodData.Count -gt 0) {
            # Top 5 by self time (excluding subcalls)
            Write-Host ""
            Write-Host "Top 5 Functions by Self Time (excluding subcalls):" -ForegroundColor Yellow
            Write-Host ""
            Write-Host ("{0,-70} {1,-12} {2,-12} {3,-10}" -f "Function", "Self Time %", "Total Time %", "Calls") -ForegroundColor Gray
            Write-Host ("{0}" -f ("-" * 110)) -ForegroundColor Gray

            $topSelf = $methodData.Values |
                Sort-Object -Property SelfTime -Descending |
                Select-Object -First 5

            foreach ($method in $topSelf) {
                $location = if ($method.SourceFile) {
                    "$($method.SourceFile):$($method.LineNumber)"
                } else {
                    $method.Method
                }
                Write-Host ("{0,-70} {1,11:F2}% {2,11:F2}% {3,10}" -f $location, $method.SelfTime, $method.TotalTime, $method.Calls)
            }

            # Top 5 by total time (including subcalls)
            Write-Host ""
            Write-Host "Top 5 Functions by Total Time (including subcalls):" -ForegroundColor Yellow
            Write-Host ""
            Write-Host ("{0,-70} {1,-12} {2,-12} {3,-10}" -f "Function", "Self Time %", "Total Time %", "Calls") -ForegroundColor Gray
            Write-Host ("{0}" -f ("-" * 110)) -ForegroundColor Gray

            $topTotal = $methodData.Values |
                Sort-Object -Property TotalTime -Descending |
                Select-Object -First 5

            foreach ($method in $topTotal) {
                $location = if ($method.SourceFile) {
                    "$($method.SourceFile):$($method.LineNumber)"
                } else {
                    $method.Method
                }
                Write-Host ("{0,-70} {1,11:F2}% {2,11:F2}% {3,10}" -f $location, $method.SelfTime, $method.TotalTime, $method.Calls)
            }

            Write-Host ""
            Write-Host ("Total methods profiled: {0}" -f $methodData.Count) -ForegroundColor Gray
        } else {
            Write-Host "No method profiling data found in hprof output." -ForegroundColor Yellow
            Write-Host "Profile file location: $profileTextFile" -ForegroundColor Gray
            Write-Host "Note: hprof may need more time to generate data. Try running longer tests." -ForegroundColor Gray
        }
        } else {
            Write-Host "Profile file is empty or could not be read." -ForegroundColor Yellow
        }

        Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Profile file: $profileTextFile" -ForegroundColor DarkGray
    } else {
        Write-Host "Profile file not generated (hprof not available)" -ForegroundColor Gray
    }
}

# Close log file stream if it was opened
Close-LogFile
