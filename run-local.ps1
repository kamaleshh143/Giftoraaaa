<#
    Starts Giftora locally on an embedded Tomcat 9, or runs the Maven build.

        .\run-local.ps1              # start the server: http://localhost:8080/
        .\run-local.ps1 -Port 9090   # use a different port
        .\run-local.ps1 -Build       # mvn clean package
        .\run-local.ps1 -Test        # run the test suite

    For the server, open the printed URL in a browser and press Ctrl+C to stop.
    The H2 database lives in .\data and survives restarts.
#>
[CmdletBinding()]
param(
    [int]$Port = 8080,
    [switch]$Build,
    [switch]$Test
)

$ErrorActionPreference = "Stop"

# Always run Maven from the project root (the folder containing this script).
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $projectDir

# --- Locate a JDK 17 -------------------------------------------------------
$candidates = @()
if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
$adoptium = "C:\Program Files\Eclipse Adoptium"
if (Test-Path $adoptium) {
    $candidates += Get-ChildItem -Path $adoptium -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like "jdk-17*" } |
        ForEach-Object { $_.FullName }
}

$jdk = $null
foreach ($candidate in $candidates) {
    if (Test-Path (Join-Path $candidate "bin\java.exe")) { $jdk = $candidate; break }
}

if ($jdk) {
    $env:JAVA_HOME = $jdk
    $env:PATH = (Join-Path $jdk "bin") + ";" + $env:PATH
}
elseif (Get-Command java -ErrorAction SilentlyContinue) {
    Write-Host "JAVA_HOME not set; using 'java' from PATH." -ForegroundColor Yellow
}
else {
    throw "No Java runtime found. Install JDK 17 or set JAVA_HOME."
}

$savedPref = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$javaVersion = (& java -version 2>&1 | Select-Object -First 1)
$ErrorActionPreference = $savedPref
Write-Host "Java:  $javaVersion"

# --- Locate Maven ----------------------------------------------------------
$mvn = $null
foreach ($candidate in @(
        "C:\tools\apache-maven-3.9.11\bin\mvn.cmd",
        "C:\tools\apache-maven-3.9.11\bin\mvn")) {
    if (Test-Path $candidate) { $mvn = $candidate; break }
}
if (-not $mvn) {
    $onPath = Get-Command mvn -ErrorAction SilentlyContinue
    if ($onPath) { $mvn = $onPath.Source }
}
if (-not $mvn) {
    throw "Maven not found. Install Maven or add it to PATH."
}

Write-Host "Maven: $mvn"

if ($Build) {
    Write-Host "Building WAR (mvn clean package)...`n" -ForegroundColor Green
    & $mvn -B clean package
    exit $LASTEXITCODE
}

if ($Test) {
    Write-Host "Running tests...`n" -ForegroundColor Green
    & $mvn -B test
    exit $LASTEXITCODE
}

Write-Host "Starting Giftora on http://localhost:$Port/  (Ctrl+C to stop)`n" -ForegroundColor Green

# --- Compile, resolve the runtime classpath, then launch a clean JVM -------
# The server must run in its own JVM (not Maven's) so the application
# classpath is clean and Ctrl+C stops it directly.
$classpathFile = Join-Path $projectDir "target\dev-classpath.txt"
& $mvn -q test-compile "dependency:build-classpath" "-Dmdep.outputFile=$classpathFile" "-Dmdep.includeScope=test"
if ($LASTEXITCODE -ne 0) { throw "Maven build failed with exit code $LASTEXITCODE." }

$cp = (Join-Path $projectDir "target\classes") + ";" + (Get-Content $classpathFile -Raw).Trim()
& java "-Dfile.encoding=UTF-8" "-Dgiftora.port=$Port" -classpath $cp com.giftora.dev.EmbeddedTomcat
