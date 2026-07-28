@echo off
set "MODS_DIR=C:\Users\loimathos\AppData\Roaming\PrismLauncher\instances\1.21.1\minecraft\mods"

echo ==========================================
echo Building Vehicle Mod 1.21.1...
echo ==========================================

call gradlew.bat jar --console=plain
if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

if not exist "%MODS_DIR%" (
    echo Creating directory: %MODS_DIR%
    mkdir "%MODS_DIR%"
)

echo.
echo Copying compiled jar to PrismLauncher mods directory...
copy /Y "build\libs\vehicle-mod-*.jar" "%MODS_DIR%\"

if errorlevel 1 (
    echo [ERROR] Failed to copy JAR file!
) else (
    echo.
    echo ==========================================
    echo [SUCCESS] Mod deployed successfully!
    echo ==========================================
)
