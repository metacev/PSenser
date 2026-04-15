# Sensor Monitor - Android App

A professional sensor monitoring application for Android 10 (API 29) to Android 16 (API 36).

## Features

### Real-time Sensor Monitoring
- Accelerometer (三轴加速度)
- Gyroscope (角速度)
- Magnetometer (磁场强度)
- Barometer (大气压力)
- Light Sensor (环境光强度)
- Proximity (距离检测)
- Humidity (相对湿度)
- Temperature (环境温度)
- And 15+ other sensors

### Hardware Status
- CPU information
- Camera status
- Microphone/Speaker status
- Battery information

### System Information
- Device model
- Android version (with code names)
- RAM usage

## Technical Specifications

| Item | Value |
|------|-------|
| Min SDK | 29 (Android 10) |
| Target SDK | 36 (Android 16) |
| Compile SDK | 36 |
| Language | Kotlin 2.0.0 |
| AGP | 8.7.0 |
| Java Version | 17 |

## Project Structure

```
MyAndroidApp/
├── gradle.properties          # Unified version management
├── build.gradle               # Project-level build config
├── settings.gradle            # Project settings
├── app/
│   ├── build.gradle           # App-level build config
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/sensormonitor/
│       │   ├── model/         # Data models
│       │   ├── ui/            # UI components
│       │   │   ├── fragments/ # Page fragments
│       │   │   └── ...
│       │   └── util/          # Utility classes
│       └── res/
│           ├── layout/        # XML layouts
│           ├── values/        # Colors, themes, strings
│           └── mipmap-*/      # App icons
└── README.md
```

## Build Instructions

1. Open the project in Android Studio Hedgehog (2023.1.1) or later
2. Sync Gradle files: File > Sync Project with Gradle Files
3. Build APK: Build > Build APK(s) or run `./gradlew assembleDebug`
4. Install on device running Android 10-16

## Permissions

The app requests the following permissions based on Android version:
- **Android 10+**: ACTIVITY_RECOGNITION
- **Android 13+**: POST_NOTIFICATIONS

All hardware features are declared as optional (`android:required="false"`) for maximum compatibility.

## UI Theme

Cyberpunk-inspired dark theme with:
- Dark background (#0D1117)
- Neon cyan accent (#00E5FF)
- Card-based layout
- Monospace fonts for data display

## License

This project is for educational purposes.
