# PowerShell script to build the mod and copy it to PrismLauncher mods directory
$ErrorActionPreference = "Stop"

$MODS_DIR = "C:\Users\loimathos\AppData\Roaming\PrismLauncher\instances\1.21.1\minecraft\mods"
$PROJECT_DIR = $PSScriptRoot

Write-Host "Building Vehicle Mod 1.21.1..." -ForegroundColor Cyan
Set-Location $PROJECT_DIR
.\gradlew jar --console=plain

if (-not (Test-Path $MODS_DIR)) {
    Write-Host "Creating mods directory: $MODS_DIR" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $MODS_DIR -Force
}

$JAR_FILE = Get-ChildItem "$PROJECT_DIR\build\libs\vehicle-mod-*.jar" | Select-Object -First 1

if ($JAR_FILE) {
    Write-Host "Copying $($JAR_FILE.Name) to $MODS_DIR..." -ForegroundColor Green
    Copy-Item -Path $JAR_FILE.FullName -Destination $MODS_DIR -Force
    Write-Host "Successfully deployed $($JAR_FILE.Name) to PrismLauncher!" -ForegroundColor Green
} else {
    Write-Host "Error: Could not find compiled JAR file in build\libs!" -ForegroundColor Red
    exit 1
}
