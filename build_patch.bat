@echo off
echo ===============================================
echo  Tinker Patch Build Script
echo  Building patch APK for hot update...
echo ===============================================

echo.
echo [Step 1] Checking if base APK exists...

if not exist "%cd%\app\build\bakApk\" (
    echo ERROR: bakApk folder does not exist!
    echo Please run build_base_apk.bat first to create the base APK.
    pause
    exit /b 1
)

rem Find the latest APK file in bakApk folder and store in temp file
dir /b /od "%cd%\app\build\bakApk\*.apk" > "%temp%\latest_apk.txt" 2>nul

rem Check if any APK files found
for /f %%i in ('type "%temp%\latest_apk.txt" ^| find /c /v ""') do set apk_count=%%i
if %apk_count%==0 (
    echo ERROR: No APK files found in bakApk folder!
    echo Please run build_base_apk.bat first to create the base APK.
    del "%temp%\latest_apk.txt" 2>nul
    pause
    exit /b 1
)

rem Get the first (latest) APK file
set /p latest_apk_name=<"%temp%\latest_apk.txt"
del "%temp%\latest_apk.txt" 2>nul

set "OLD_APK=%cd%\app\build\bakApk\%latest_apk_name%"

echo Found latest APK: %latest_apk_name%

rem Extract base filename without extension
set "base_filename=%latest_apk_name:.apk=%"
set "APPLY_MAPPING=%cd%\app\build\bakApk\%base_filename%-mapping.txt"
set "APPLY_RESOURCE=%cd%\app\build\bakApk\%base_filename%-R.txt"

echo OLD_APK: %OLD_APK%
echo APPLY_MAPPING: %APPLY_MAPPING%
echo APPLY_RESOURCE: %APPLY_RESOURCE%

if not exist "%OLD_APK%" (
    echo ERROR: Base APK not found at: %OLD_APK%
    echo Please run build_base_apk.bat first to create the base APK.
    pause
    exit /b 1
)

echo Base APK found: %OLD_APK%

echo.
echo [Step 2] Building patch with Tinker enabled...

echo Using base APK: %OLD_APK%

if exist "%APPLY_MAPPING%" (
    echo Using mapping file: %APPLY_MAPPING%
    call gradlew tinkerPatchDebug -PTINKER_ENABLE=true -POLD_APK="%OLD_APK%" -PAPPLY_MAPPING="%APPLY_MAPPING%" -PAPPLY_RESOURCE="%APPLY_RESOURCE%"
) else (
    echo No mapping file found, building without mapping...
    call gradlew tinkerPatchDebug -PTINKER_ENABLE=true -POLD_APK="%OLD_APK%" -PAPPLY_RESOURCE="%APPLY_RESOURCE%"
)

echo.
echo [Step 3] Checking patch generation...
if exist ".\app\build\outputs\apk\tinkerPatch\debug\app-debug-patch_signed_7zip.apk" (
    echo ===============================================
    echo  Patch built successfully!
    echo  Location: .\app\build\outputs\apk\tinkerPatch\debug\app-debug-patch_signed_7zip.apk
    echo  To apply the patch:
    echo  1. Create app directory: adb shell mkdir -p /storage/emulated/0/Android/data/tinker.sample.android/files/
    echo  2. Push patch to device: adb push .\app\build\outputs\apk\tinkerPatch\debug\app-debug-patch_signed_7zip.apk /storage/emulated/0/Android/data/tinker.sample.android/files/
    echo  3. Apply patch in your app by clicking the "Load Patch" button
    echo  Auto-pushing patch to device...
    adb shell mkdir -p /storage/emulated/0/Android/data/tinker.sample.android/files/ 
    adb shell rm /storage/emulated/0/Android/data/tinker.sample.android/files/app-debug-patch_signed_7zip.apk
    adb push .\app\build\outputs\apk\tinkerPatch\debug\app-debug-patch_signed_7zip.apk /storage/emulated/0/Android/data/tinker.sample.android/files/
    echo ===============================================
) else (
    echo ERROR: Patch generation failed!
    echo Check the build logs for details.
)

echo.