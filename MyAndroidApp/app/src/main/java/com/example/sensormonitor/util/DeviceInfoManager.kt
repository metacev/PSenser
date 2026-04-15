package com.example.sensormonitor.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import com.example.sensormonitor.model.*
import java.io.File

/**
 * Utility class for collecting device hardware information
 */
class DeviceInfoManager(private val context: Context) {
    
    /**
     * Get CPU information
     */
    fun getCpuInfo(): CpuInfo {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val frequencies = mutableListOf<Long>()
        
        // Read CPU frequencies from sysfs
        for (i in 0 until coreCount) {
            try {
                val freqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                if (freqFile.exists()) {
                    val freq = freqFile.readText().trim().toLongOrNull() ?: 0L
                    frequencies.add(freq / 1000) // Convert to MHz
                } else {
                    frequencies.add(0L)
                }
            } catch (e: Exception) {
                frequencies.add(0L)
            }
        }
        
        // Get CPU temperature if available
        var temperature = 0f
        try {
            val tempFile = File("/sys/class/thermal/thermal_zone0/temp")
            if (tempFile.exists()) {
                temperature = tempFile.readText().trim().toFloatOrNull()?.div(1000) ?: 0f
            }
        } catch (e: Exception) {
            temperature = 0f
        }
        
        return CpuInfo(
            coreCount = coreCount,
            frequencies = frequencies,
            usagePercent = 0f, // Requires more complex calculation
            temperature = temperature
        )
    }
    
    /**
     * Get battery information
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun getBatteryInfo(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = intent?.let {
            val current = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (current >= 0 && scale > 0) {
                (current * 100 / scale)
            } else {
                0
            }
        } ?: 0
        
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.div(10f) ?: 0f
        
        val isCharging = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING,
            BatteryManager.BATTERY_STATUS_FULL -> true
            else -> false
        }
        
        val chargeType = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        
        return BatteryInfo(
            level = level,
            status = status,
            health = health,
            voltage = voltage,
            temperature = temperature,
            isCharging = isCharging,
            chargeType = chargeType
        )
    }
    
    /**
     * Get camera status information
     */
    fun getCameraStatusList(): List<CameraStatus> {
        val cameraList = mutableListOf<CameraStatus>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraIdList = cameraManager.cameraIdList
                
                for (cameraId in cameraIdList) {
                    try {
                        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                        val facing = characteristics.get(
                            android.hardware.camera2.CameraCharacteristics.LENS_FACING
                        ) ?: 0
                        val orientation = characteristics.get(
                            android.hardware.camera2.CameraCharacteristics.SENSOR_ORIENTATION
                        ) ?: 0
                        
                        cameraList.add(
                            CameraStatus(
                                cameraId = cameraId,
                                isAvailable = true,
                                facing = facing,
                                orientation = orientation
                            )
                        )
                    } catch (e: Exception) {
                        cameraList.add(
                            CameraStatus(
                                cameraId = cameraId,
                                isAvailable = false,
                                facing = 0,
                                orientation = 0
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Camera access not available
            }
        }
        
        return cameraList
    }
    
    /**
     * Get audio device status
     */
    fun getAudioDeviceStatus(): AudioDeviceStatus {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val isMuted = currentVolume == 0
        
        // Check microphone availability
        val isMicrophoneAvailable = context.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_MICROPHONE
        )
        
        // Check speaker availability
        val isSpeakerAvailable = context.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_AUDIO_OUTPUT
        )
        
        return AudioDeviceStatus(
            isMicrophoneAvailable = isMicrophoneAvailable,
            isSpeakerAvailable = isSpeakerAvailable,
            currentVolume = currentVolume,
            maxVolume = maxVolume,
            isMuted = isMuted
        )
    }
    
    /**
     * Get device summary information
     */
    fun getDeviceSummary(): DeviceSummary {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val availableMemory = totalMemory - freeMemory
        
        return DeviceSummary(
            deviceName = Build.DEVICE,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            totalMemory = totalMemory,
            availableMemory = availableMemory
        )
    }
}
