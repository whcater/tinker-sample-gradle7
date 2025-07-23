#!/bin/bash

echo "==============================================="
echo "  Tinker Patch Build Script"
echo "  Building patch APK for hot update..."
echo "==============================================="

echo
echo "[Step 1] Checking if base APK exists..."

if [ ! -d "app/build/bakApk" ]; then
    echo "ERROR: bakApk folder does not exist!"
    echo "Please run build_base_apk.sh first to create the base APK."
    read -p "Press any key to continue..."
    exit 1
fi

# Find the latest APK file in bakApk folder
latest_apk_name=$(find app/build/bakApk -name "*.apk" -type f -exec stat -f "%m %N" {} + 2>/dev/null | sort -n | head -1 | awk '{print $NF}' | xargs basename 2>/dev/null)

if [ -z "$latest_apk_name" ]; then
    echo "ERROR: No APK files found in bakApk folder!"
    echo "Please run build_base_apk.sh first to create the base APK."
    read -p "Press any key to continue..."
    exit 1
fi

OLD_APK="$(pwd)/app/build/bakApk/$latest_apk_name"

echo "Found latest APK: $latest_apk_name"

# Extract base filename without extension
base_filename="${latest_apk_name%.apk}"
APPLY_MAPPING="$(pwd)/app/build/bakApk/${base_filename}-mapping.txt"
APPLY_RESOURCE="$(pwd)/app/build/bakApk/${base_filename}-R.txt"

echo "OLD_APK: $OLD_APK"
echo "APPLY_MAPPING: $APPLY_MAPPING"
echo "APPLY_RESOURCE: $APPLY_RESOURCE"

if [ ! -f "$OLD_APK" ]; then
    echo "ERROR: Base APK not found at: $OLD_APK"
    echo "Please run build_base_apk.sh first to create the base APK."
    read -p "Press any key to continue..."
    exit 1
fi

echo "Base APK found: $OLD_APK"

echo
echo "[Step 2] Building patch with Tinker enabled..."

echo "Using base APK: $OLD_APK"

if [ -f "$APPLY_MAPPING" ]; then
    echo "Using mapping file: $APPLY_MAPPING"
    ./gradlew tinkerPatchDebug -PTINKER_ENABLE=true -POLD_APK="$OLD_APK" -PAPPLY_MAPPING="$APPLY_MAPPING" -PAPPLY_RESOURCE="$APPLY_RESOURCE"
else
    echo "No mapping file found, building without mapping..."
    ./gradlew tinkerPatchDebug -PTINKER_ENABLE=true -POLD_APK="$OLD_APK" -PAPPLY_RESOURCE="$APPLY_RESOURCE"
fi

echo
echo "[Step 3] Checking patch generation..."
if [ -f "./app/build/outputs/apk/tinkerPatch/debug/app-debug-patch_signed_7zip.apk" ]; then
    echo "==============================================="
    echo "  Patch built successfully!"  Patch directory: /data/user/0/tinker.sample.android/tinker
    echo "  Location: ./app/build/outputs/apk/tinkerPatch/debug/app-debug-patch_signed_7zip.apk"
    echo "  To apply the patch:"
    echo "  1. Create app directory: adb shell mkdir -p /storage/emulated/0/Android/data/tinker.sample.android/files/"
    echo "  2. Push patch to device: adb push ./app/build/outputs/apk/tinkerPatch/debug/app-debug-patch_signed_7zip.apk /storage/emulated/0/Android/data/tinker.sample.android/files/"
    echo "  3. Apply patch in your app by clicking the \"Load Patch\" button"
    echo "  Auto-pushing patch to device..."
    adb shell mkdir -p /storage/emulated/0/Android/data/tinker.sample.android/files/
    adb shell rm /storage/emulated/0/Android/data/tinker.sample.android/files/app-debug-patch_signed_7zip.apk 2>/dev/null
    adb push ./app/build/outputs/apk/tinkerPatch/debug/app-debug-patch_signed_7zip.apk /storage/emulated/0/Android/data/tinker.sample.android/files/
    echo "==============================================="
else
    echo "ERROR: Patch generation failed!"
    echo "Check the build logs for details."
fi

echo