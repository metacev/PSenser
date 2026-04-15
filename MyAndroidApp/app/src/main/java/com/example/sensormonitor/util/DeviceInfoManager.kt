package com.example.sensormonitor.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import com.example.sensormonitor.model.*

/**
 * Device Info Manager Utility - Handles device hardware information
 */
class DeviceInfoManager(private val context: Context) {

    // Get device basic info
    fun getDeviceInfo(): DeviceInfo {
        val totalMemory = Runtime.getRuntime().totalMemory()
        val freeMemory = Runtime.getRuntime().freeMemory()
        
        return DeviceInfo(
            deviceName = Build.DEVICE,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = getAndroidVersionName(Build.VERSION.SDK_INT),
            sdkInt = Build.VERSION.SDK_INT,
            buildNumber = Build.DISPLAY,
            totalMemory = totalMemory,
            availableMemory = totalMemory - freeMemory
        )
    }

    // Get Android version name
    fun getAndroidVersionName(sdkInt: Int): String {
        return when {
            sdkInt >= 36 -> "Android 16"
            sdkInt >= 35 -> "Android 15 (Vanilla Ice Cream)"
            sdkInt >= 34 -> "Android 14 (Upside Down Cake)"
            sdkInt >= 33 -> "Android 13 (Tiramisu)"
            sdkInt >= 32 -> "Android 12L (Snow Cone v2)"
            sdkInt >= 31 -> "Android 12 (Snow Cone)"
            sdkInt >= 30 -> "Android 11 (Red Velvet Cake)"
            sdkInt >= 29 -> "Android 10 (Quince Tart)"
            else -> "Android $sdkInt"
        }
    }

    // Get battery info
    fun getBatteryInfo(): BatteryInfo? {
        try {
            val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryIntent ?: return null

            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level != -1 && scale != -1) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else -1

            val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val statusString = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                else -> "Unknown"
            }

            val health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val healthString = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }

            val voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f
            
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                            status == BatteryManager.BATTERY_STATUS_FULL
            
            val plugType = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargerType = when (plugType) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "None"
            }

            return BatteryInfo(
                level = batteryPct,
                status = statusString,
                health = healthString,
                voltage = voltage,
                temperature = temperature,
                isCharging = isCharging,
                chargerType = chargerType
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Get CPU info
    fun getCpuInfo(): CpuInfo {
        val coreCount = Runtime.getRuntime().availableProcessors()
        
        // Try to read CPU frequency from system (may require root on some devices)
        var currentFreq = "N/A"
        var maxFreq = "N/A"
        var minFreq = "N/A"
        var temperature: Float? = null

        try {
            // Read CPU freq (works on some devices without root)
            val freqFile = java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (freqFile.exists()) {
                currentFreq = "${(freqFile.readText().trim().toIntOrNull() ?: 0) / 1000} MHz"
            }
            
            val maxFreqFile = java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq")
            if (maxFreqFile.exists()) {
                maxFreq = "${(maxFreqFile.readText().trim().toIntOrNull() ?: 0) / 1000} MHz"
            }
            
            val minFreqFile = java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq")
            if (minFreqFile.exists()) {
                minFreq = "${(minFreqFile.readText().trim().toIntOrNull() ?: 0) / 1000} MHz"
            }

            // Try to read CPU temperature
            val tempFile = java.io.File("/sys/class/thermal/thermal_zone0/temp")
            if (tempFile.exists()) {
                temperature = (tempFile.readText().trim().toIntOrNull() ?: 0) / 1000.0f
            }
        } catch (e: Exception) {
            // File access may fail on some devices
        }

        return CpuInfo(
            coreCount = coreCount,
            currentFreq = currentFreq,
            maxFreq = maxFreq,
            minFreq = minFreq,
            temperature = temperature,
            usage = null // Requires more complex calculation
        )
    }

    // Get camera info
    fun getCameraInfo(): CameraInfo {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val hasFrontCamera = cameraManager.cameraIdList.any { id ->
            try {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) == 
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT
            } catch (e: Exception) {
                false
            }
        }

        val hasBackCamera = cameraManager.cameraIdList.any { id ->
            try {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) == 
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
            } catch (e: Exception) {
                false
            }
        }

        return CameraInfo(
            hasFrontCamera = hasFrontCamera,
            hasBackCamera = hasBackCamera,
            cameraCount = cameraManager.cameraIdList.size,
            cameraIds = cameraManager.cameraIdList.toList()
        )
    }

    // Get audio info
    fun getAudioInfo(): AudioInfo {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val hasMicrophone = context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        val hasSpeaker = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        
        val mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        val alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        return AudioInfo(
            hasMicrophone = hasMicrophone,
            hasSpeaker = hasSpeaker,
            mediaVolume = mediaVolume,
            ringVolume = ringVolume,
            alarmVolume = alarmVolume,
            maxVolume = maxVolume
        )
    }
}
