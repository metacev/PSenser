package com.example.sensormonitor.model

/**
 * Sensor data model representing real-time sensor readings
 */
data class SensorData(
    val sensorType: Int,
    val sensorName: String,
    val values: FloatArray,
    val timestamp: Long = System.currentTimeMillis(),
    val unit: String = ""
) {
    fun getValueString(): String {
        return values.joinToString(", ") { String.format("%.2f", it) }
    }
}

/**
 * CPU information model
 */
data class CpuInfo(
    val coreCount: Int,
    val frequencies: List<Long>,
    val usagePercent: Float,
    val temperature: Float = 0f
)

/**
 * Battery information model
 */
data class BatteryInfo(
    val level: Int,
    val status: Int,
    val health: Int,
    val voltage: Int,
    val temperature: Float,
    val isCharging: Boolean,
    val chargeType: String = ""
)

/**
 * Camera status model
 */
data class CameraStatus(
    val cameraId: String,
    val isAvailable: Boolean,
    val facing: Int,
    val orientation: Int
)

/**
 * Audio device status model
 */
data class AudioDeviceStatus(
    val isMicrophoneAvailable: Boolean,
    val isSpeakerAvailable: Boolean,
    val currentVolume: Int,
    val maxVolume: Int,
    val isMuted: Boolean
)

/**
 * Device info summary
 */
data class DeviceSummary(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val totalMemory: Long,
    val availableMemory: Long
)
