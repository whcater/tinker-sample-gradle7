@echo off
setlocal enabledelayedexpansion
echo ===============================================
echo  Tinker Base APK Build Script
echo  Building base APK for hot update...
echo ===============================================

echo.
echo [Step 1] Cleaning previous builds...
call gradlew clean

echo.
echo [Step 2] Building base APK with Tinker enabled...
call gradlew assembleDebug -PTINKER_ENABLE=true

echo.
echo [Step 4] Finding and installing latest APK from bakApk folder...

rem Find the latest APK file in bakApk folder using a simpler method
set "latest_apk="
for /f "delims=" %%f in ('dir /b /o-d "app\build\bakApk\*.apk" 2^>nul') do (
    if not defined latest_apk (
        set "latest_apk=app\build\bakApk\%%f"
    )
)

if not defined latest_apk (
    echo ERROR: No APK files found in bakApk folder!
    pause
    exit /b 1
)

echo Found latest APK: !latest_apk!
echo Installing APK to device...

adb install -r "!latest_apk!"
if errorlevel 1 (
    echo ERROR: Failed to install APK!
    echo Please check:
    echo 1. ADB is installed and in PATH
    echo 2. Device is connected and USB debugging is enabled  
    echo 3. Device has sufficient storage space
    pause
    exit /b 1
) else (
    echo APK installed successfully!
)

echo.
echo ===============================================
echo  Base APK build and installation completed!
echo  Installed APK: !latest_apk!
echo  Next steps:
echo  1. Make code changes for patch
echo  2. Run build_patch.bat to generate patch
echo ===============================================
pause

