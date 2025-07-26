# Tinker Sample Android Project

A sample Android application demonstrating the integration and usage of [Tencent Tinker](https://github.com/Tencent/tinker), a hot-fix solution library for Android.

## Overview

This project showcases how to implement hot-fix capabilities in Android applications using Tinker, allowing for dynamic patching of APKs without requiring users to download and install full application updates.

## Features

- **Hot-fix Support**: Dynamic patching using Tencent Tinker
- **Multi-dex Configuration**: Support for applications with multiple DEX files
- **Proguard Integration**: Code obfuscation and optimization
- **Debug and Release Builds**: Complete build configuration for both development and production
- **Patch Management**: Automated patch generation and application

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 29 or higher
- Java 8
- Git (for generating Tinker IDs)

## Project Structure

```
app/
├── src/main/java/tinker/sample/android/
│   ├── app/                    # Application classes and build info
│   ├── crash/                  # Exception handling
│   ├── Log/                    # Logging implementation
│   ├── reporter/               # Tinker reporting and listeners
│   ├── service/                # Background services
│   └── util/                   # Utility classes and Tinker manager
├── keystore/                   # Debug and release keystores
└── build.gradle               # App-level build configuration
```

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd tinker-sample-gradle7
```

### 2. Build Base APK

#### Using Gradle:
```bash
./gradlew assembleDebug
```

#### Using Build Scripts:
- **Windows**: `build_base_apk.bat`
- **Linux/Mac**: `build_base_apk.sh`

### 3. Generate Patches

After making code changes:

#### Using Gradle:
```bash
./gradlew tinkerPatchDebug -POLD_APK=<path-to-old-apk> -PAPPLY_MAPPING=<path-to-mapping> -PAPPLY_RESOURCE=<path-to-resources>
```

#### Using Build Scripts:
- **Windows**: `build_patch.bat`
- **Linux/Mac**: `build_patch.sh`

## Configuration

### Tinker Configuration

The Tinker configuration is defined in `app/build.gradle`:

- **Application ID**: `tinker.sample.android`
- **Tinker Version**: `1.9.15.2`
- **Min SDK**: 29
- **Target SDK**: 33

### Key Configuration Files

- `gradle.properties`: Global project settings and Tinker version
- `app/build.gradle`: Application build configuration and Tinker setup
- `proguard-rules.pro`: ProGuard configuration
- `tinker_multidexkeep.pro`: Multi-dex keep rules

## Build Variants

### Debug Build
- Debuggable: `true`
- Minify Enabled: `true`
- Signing: Debug keystore
- Multi-dex: Enabled

### Release Build
- Minify Enabled: `true`
- Signing: Release keystore (`keystore/release.keystore`)
- Multi-dex: Enabled
- ProGuard: Enabled

## Patch Process

1. **Build Base APK**: Create the initial application version
2. **Make Changes**: Modify code, resources, or assets
3. **Generate Patch**: Use Tinker to create a patch file
4. **Apply Patch**: Deploy the patch to update the application

## Important Notes

- Ensure Git is installed and accessible from command line for automatic Tinker ID generation
- The loader classes specified in the Tinker configuration cannot be changed via patches
- Resources changes require proper mapping files for successful patching
- Always test patches thoroughly before deployment

## Logging and Debugging

The project includes comprehensive logging through:
- Custom logging implementation (`MyLogImp.java`)
- Tinker reporting system
- Crash handling (`SampleUncaughtExceptionHandler.java`)

## Scripts

- `trace_log.sh` / `trace_log.ps1`: Log tracing utilities
- `updateTinkerLib.sh`: Tinker library update script
- `fix_android_build_permission.sh`: Permission fix for Android builds

## License

This project is a sample implementation for educational purposes. Please refer to the [Tencent Tinker license](https://github.com/Tencent/tinker/blob/master/LICENSE) for the underlying framework.