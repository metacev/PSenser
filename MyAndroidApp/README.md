# Sensor Monitor - Android 10+ Device Monitoring App

## 📱 Project Overview

A professional Android application designed for Android 10 (API 29) and above that displays real-time information from all device sensors and hardware components.

## ✨ Features

### Real-time Sensor Monitoring
- **Accelerometer** - Motion detection in 3 axes (m/s²)
- **Gyroscope** - Rotation rate measurement (rad/s)
- **Magnetometer** - Magnetic field strength (μT)
- **Barometer** - Atmospheric pressure (hPa)
- **Light Sensor** - Ambient light levels (lux)
- **Proximity Sensor** - Distance detection (cm)
- **Humidity Sensor** - Relative humidity (%)
- **Temperature Sensor** - Ambient temperature (°C)
- **Gravity Sensor** - Gravity force measurement
- **Linear Acceleration** - Acceleration without gravity
- **Rotation Vector** - Device orientation
- **And more...**

### Device Hardware Information
- **CPU Status**
  - Core count
  - Real-time frequencies per core
  - CPU temperature
  
- **Battery Information**
  - Current charge level (%)
  - Charging status
  - Battery health
  - Voltage (mV)
  - Temperature (°C)

- **Camera Status**
  - Available cameras (front/back/external)
  - Camera availability status
  - Orientation information

- **Audio Devices**
  - Microphone availability
  - Speaker availability
  - Volume levels
  - Mute status

- **Device Information**
  - Manufacturer & Model
  - Android version
  - SDK version
  - Memory usage

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **Min SDK**: Android 10 (API 29)
- **Target SDK**: API 34
- **UI Framework**: Material Design 3
- **Architecture**: MVVM with Fragment-based navigation

### Key Technologies
- AndroidX Libraries
- Material Components
- ViewPager2 with TabLayout
- Coroutines for async operations
- Kotlin Flow for reactive data streams
- ViewBinding for type-safe view access

### Project Structure
```
app/src/main/java/com/example/sensormonitor/
├── model/          # Data models
│   └── SensorModels.kt
├── ui/             # UI components
│   ├── MainActivity.kt
│   ├── SensorListFragment.kt
│   ├── SensorDetailFragment.kt
│   └── DeviceInfoFragment.kt
├── util/           # Utility classes
│   ├── SensorDataManager.kt
│   ├── DeviceInfoManager.kt
│   └── PermissionHelper.kt
└── ...
```

## 🎨 UI Design

### Color Scheme (Dark Cyber Theme)
- Primary: Cyan (#00BCD4)
- Accent: Neon Cyan (#00E5FF)
- Background: Dark (#0D1117)
- Cards: Dark Gray (#161B22)

### Design Principles
- Clean, modern interface
- High contrast for readability
- Card-based layout
- Real-time value highlighting
- Monospace fonts for numerical data

## 🔧 Build Configuration

### Unified Version Management
All versions are centralized in `gradle.properties`:
- SDK versions
- Dependency versions
- Build tool versions

This prevents version conflicts and ensures consistency across the project.

### Dependencies
- AndroidX AppCompat
- Material Components
- ConstraintLayout
- Lifecycle Components
- Kotlin Coroutines
- MPAndroidChart (for future visualization)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 8 or higher
- Android SDK 29+

### Installation Steps

1. **Clone/Open Project**
   ```
   Open the MyAndroidApp folder in Android Studio
   ```

2. **Sync Gradle**
   ```
   File > Sync Project with Gradle Files
   ```

3. **Build APK**
   ```
   Build > Build APK(s)
   ```
   Or use command line:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Device**
   - Connect Android 10+ device via USB
   - Enable USB debugging
   - Run > Run 'app'

## 📋 Permissions

The app requests the following runtime permissions:
- `CAMERA` - To check camera status
- `RECORD_AUDIO` - To check microphone status
- `ACTIVITY_RECOGNITION` - For activity sensors (Android 10+)

## ⚠️ Notes

- Some sensors may not be available on all devices
- CPU temperature reading requires root access on some devices
- Camera access is read-only (no actual camera capture)
- Audio monitoring is status-only (no recording)

## 📄 License

This project is provided as-is for educational purposes.

## 🤝 Contributing

Feel free to enhance this project with:
- Additional sensor support
- Chart visualizations
- Data logging features
- Export functionality
