package com.example.sensormonitor.model

/**
 * Sensor data model class
 */
data class SensorData(
    val name: String,
    val value: String,
    val unit: String,
    val accuracy: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Device info model class
 */
data class DeviceInfo(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val buildNumber: String,
    val totalMemory: Long,
    val availableMemory: Long
)

/**
 * Battery info model class
 */
data class BatteryInfo(
    val level: Int,
    val status: String,
    val health: String,
    val voltage: Int,
    val temperature: Float,
    val isCharging: Boolean,
    val chargerType: String
)

/**
 * CPU info model class
 */
data class CpuInfo(
    val coreCount: Int,
    val currentFreq: String,
    val maxFreq: String,
    val minFreq: String,
    val temperature: Float? = null,
    val usage: Float? = null
)

/**
 * Camera info model class
 */
data class CameraInfo(
    val hasFrontCamera: Boolean,
    val hasBackCamera: Boolean,
    val cameraCount: Int,
    val cameraIds: List<String>
)

/**
 * Audio info model class
 */
data class AudioInfo(
    val hasMicrophone: Boolean,
    val hasSpeaker: Boolean,
    val mediaVolume: Int,
    val ringVolume: Int,
    val alarmVolume: Int,
    val maxVolume: Int
)
