package com.example.sensormonitor.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import kotlin.math.roundToInt

class DeviceInfoManager(private val context: Context) {

    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun getManufacturer(): String {
        return Build.MANUFACTURER
    }

    fun getAndroidVersionName(): String {
        return when (Build.VERSION.SDK_INT) {
            36 -> "Android 16"
            35 -> "Android 15 (Vanilla Ice Cream)"
            34 -> "Android 14 (Upside Down Cake)"
            33 -> "Android 13 (Tiramisu)"
            32 -> "Android 12L (Snow Cone v2)"
            31 -> "Android 12 (Snow Cone)"
            30 -> "Android 11 (Red Velvet Cake)"
            29 -> "Android 10 (Quince Tart)"
            else -> "Android ${Build.VERSION.RELEASE}"
        }
    }

    fun getRamInfo(): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val totalRam = memInfo.totalMem / (1024 * 1024)
        val availableRam = memInfo.availMem / (1024 * 1024)
        val usedRam = totalRam - availableRam
        val percent = (usedRam.toDouble() / totalRam * 100).roundToInt()
        
        return "${usedRam}MB / ${totalRam}MB ($percent%)"
    }

    fun getCameraStatus(): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList
            if (cameraIds.isEmpty()) {
                "No cameras available"
            } else {
                "${cameraIds.size} camera(s) available"
            }
        } catch (e: Exception) {
            "Camera info unavailable"
        }
    }

    @SuppressLint("MissingPermission")
    fun getAudioStatus(): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val isMicAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
            val isSpeakerAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
            
            val micStatus = if (isMicAvailable) "✓" else "✗"
            val speakerStatus = if (isSpeakerAvailable) "✓" else "✗"
            
            "Mic: $micStatus | Speaker: $speakerStatus"
        } catch (e: Exception) {
            "Audio info unavailable"
        }
    }

    fun getBatteryInfo(): BatteryInfo {
        // This would require a BroadcastReceiver in real implementation
        // For now, return placeholder data
        return BatteryInfo(
            level = 0,
            isCharging = false,
            temperature = 0.0f,
            voltage = 0,
            health = "Unknown"
        )
    }
}

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val voltage: Int,
    val health: String
)
