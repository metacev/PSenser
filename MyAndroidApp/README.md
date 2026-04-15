# Sensor Monitor App

A professional Android application for monitoring all device sensors and hardware status in real-time.

## 📱 Features

### Real-time Sensor Monitoring
- **Accelerometer** - 3-axis acceleration (m/s²)
- **Gyroscope** - Angular velocity (rad/s)
- **Magnetometer** - Magnetic field strength (μT)
- **Barometer** - Atmospheric pressure (hPa)
- **Light Sensor** - Ambient light (lux)
- **Proximity Sensor** - Distance detection (cm)
- **Humidity Sensor** - Relative humidity (%)
- **Temperature Sensor** - Ambient temperature (°C)
- **Gravity Sensor** - Gravity force (m/s²)
- **Linear Acceleration** - Linear acceleration without gravity
- **Rotation Vector** - Device orientation
- **Step Counter** - Step counting
- **And more...**

### Hardware Status
- **CPU**: Core count, frequency, temperature
- **Battery**: Level, status, health, voltage, temperature, charger type
- **Camera**: Front/back camera availability, camera IDs
- **Audio**: Microphone/speaker status, volume levels
- **Device Info**: Model, manufacturer, Android version, memory usage

## 🔧 Technical Specifications

### Compatibility
- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 16 (API 36)
- **Compile SDK**: API 36

### Unified Version Management
All versions are centralized in `gradle.properties`:
- SDK versions
- Build tools version
- Dependency versions
- Java version

### Permissions
- Activity Recognition (Android 10+)
- Bluetooth Connect (Android 12+)
- Post Notifications (Android 13+)
- Camera (optional, for status check only)
- Record Audio (optional, for status check only)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 36

### Installation

1. **Open Project**
   ```
   File > Open > Select MyAndroidApp folder
   ```

2. **Sync Gradle**
   ```
   File > Sync Project with Gradle Files
   ```

3. **Build APK**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use: `Build > Build APK(s)`

4. **Run on Device**
   - Connect Android 10+ device with USB debugging enabled
   - Click Run button or press Shift+F10

## 📁 Project Structure

```
MyAndroidApp/
├── gradle.properties          # Unified version configuration
├── build.gradle               # Project-level build config
├── settings.gradle            # Project settings
├── app/
│   ├── build.gradle           # App-level build config
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/sensormonitor/
│       │   ├── model/         # Data models
│       │   │   └── DataModels.kt
│       │   ├── ui/            # UI components
│       │   │   └── MainActivity.kt
│       │   └── util/          # Utility classes
│       │       ├── SensorManagerUtil.kt
│       │       └── DeviceInfoManager.kt
│       └── res/
│           ├── layout/        # UI layouts
│           ├── values/        # Colors, strings, themes
│           ├── drawable/      # Icons and graphics
│           └── mipmap-*/      # App icons
└── README.md
```

## 🎨 UI Design

The app features a modern cyberpunk-inspired dark theme:
- **Background**: Deep dark (#0D1117)
- **Cards**: Slightly lighter (#161B22)
- **Accent Colors**: Neon cyan, green, orange, purple, pink
- **Text**: High contrast white/gray
- **Font**: Monospace for data display

## ⚠️ Notes

- Some sensors may not be available on all devices
- CPU temperature reading may require root access on some devices
- Camera and microphone permissions are optional (used for status check only)
- The app automatically handles missing sensors gracefully

## 📄 License

This project is provided as-is for educational and demonstration purposes.

## 🔄 Updates

### Version 1.0
- Initial release
- Support for Android 10-16
- Real-time sensor monitoring
- Hardware status display
- Unified version management
- Modern Material Design 3 UI
