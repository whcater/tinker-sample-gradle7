#!/bin/bash

echo "==============================================="
echo "  Tinker Base APK Build Script"
echo "  Building base APK for hot update..."
echo "==============================================="

echo
echo "[Step 1] Cleaning previous builds..."
./gradlew clean

echo
echo "[Step 2] Building base APK with Tinker enabled..."
# ./gradlew assembleDebug -PTINKER_ENABLE=false
./gradlew assembleDebug 

echo
echo "[Step 4] Finding and installing latest APK from bakApk folder..."

# Find the latest APK file in bakApk folder
latest_apk=""
if [ -d "app/build/bakApk" ]; then
    latest_apk=$(find app/build/bakApk -name "*.apk" -type f -exec stat -f "%m %N" {} + 2>/dev/null | sort -rn | head -1 | cut -d' ' -f2-)
fi

 if [ -z "$latest_apk" ]; then
     echo "ERROR: No APK files found in bakApk folder!"
     read -p "Press any key to continue..."
     exit 1
 fi

echo "Found latest APK: $latest_apk"
echo "Installing APK to device..."

 adb install -r "$latest_apk"
#adb install -r app/build/outputs/apk/debug/app-debug.apk
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to install APK!" 
    echo "Please check:"
    echo "1. ADB is installed and in PATH"
    echo "2. Device is connected and USB debugging is enabled"
    echo "3. Device has sufficient storage space"
    read -p "Press any key to continue..."
    exit 1
else
    echo "APK installed successfully!"
fi

echo
echo "==============================================="
echo "  Base APK build and installation completed!"
echo "  Installed APK: $latest_apk"
echo "  Next steps:"
echo "  1. Make code changes for patch"
echo "  2. Run build_patch.sh to generate patch"
echo "==============================================="
# read -p "Press any key to continue..."