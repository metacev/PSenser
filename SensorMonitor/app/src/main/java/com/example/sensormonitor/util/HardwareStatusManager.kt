package com.example.sensormonitor.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import java.io.File

class HardwareStatusManager(private val context: Context) {

    // ==================== 电池信息 ====================
    fun getBatteryLevel(): String {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level == -1 || scale == -1) return "未知"
        val percentage = level * 100 / scale
        return "$percentage%"
    }

    fun getBatteryLevelPercent(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level == -1 || scale == -1) return 0
        return level * 100 / scale
    }

    fun getBatteryStatus(): String {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
            BatteryManager.BATTERY_STATUS_FULL -> "已充满"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "未知"
            else -> "未知"
        }
    }

    fun getBatteryHealth(): String {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
            BatteryManager.BATTERY_HEALTH_DEAD -> "损坏"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "过压"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "故障"
            BatteryManager.BATTERY_HEALTH_COLD -> "过冷"
            else -> "未知"
        }
    }

    fun getBatteryTemperature(): String {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        if (temp == -1) return "未知"
        return "${temp / 10.0}°C"
    }

    fun getBatteryVoltage(): String {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        if (voltage == -1) return "未知"
        return "${voltage / 1000.0}V"
    }

    // ==================== CPU 状态 ====================
    fun getCpuUsage(): String {
        // Android 无法直接获取 CPU 使用率，返回核心数
        return "${Runtime.getRuntime().availableProcessors()} 核心活跃"
    }

    fun getCpuFrequency(): String {
        try {
            val cpuInfoFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (cpuInfoFile.exists()) {
                val freq = cpuInfoFile.readText().trim().toLongOrNull() ?: return "未知"
                return "${freq / 1000} MHz"
            }
        } catch (e: Exception) {
            // 需要 root 权限或设备不支持
        }
        return "受限访问"
    }

    fun getCpuTemperature(): String {
        try {
            // 尝试读取 CPU 温度 (需要 root 权限或特定设备支持)
            val tempFiles = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
            )
            for (file in tempFiles) {
                val f = File(file)
                if (f.exists()) {
                    val temp = f.readText().trim().toLongOrNull() ?: continue
                    return "${temp / 1000.0}°C"
                }
            }
        } catch (e: Exception) {
            // 需要 root 权限
        }
        return "受限访问"
    }

    // ==================== 相机状态 ====================
    fun getCameraCount(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            cameraManager.cameraIdList.size
        } else {
            0
        }
    }

    fun getFrontCameraStatus(): String {
        return if (hasFrontCamera()) "可用" else "不可用"
    }

    fun getBackCameraStatus(): String {
        return if (hasBackCamera()) "可用" else "不可用"
    }

    private fun hasFrontCamera(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            cameraManager.cameraIdList.any { id ->
                try {
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                    facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT
                } catch (e: Exception) {
                    false
                }
            }
        } else {
            false
        }
    }

    private fun hasBackCamera(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            cameraManager.cameraIdList.any { id ->
                try {
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                    facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
                } catch (e: Exception) {
                    false
                }
            }
        } else {
            false
        }
    }

    // ==================== 音频状态 ====================
    fun getMicrophoneStatus(): String {
        val packageManager = context.packageManager
        return if (packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_MICROPHONE)) {
            "可用"
        } else {
            "不可用"
        }
    }

    fun getSpeakerStatus(): String {
        val packageManager = context.packageManager
        return if (packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_AUDIO_OUTPUT)) {
            "可用"
        } else {
            "不可用"
        }
    }

    fun getVolumeLevel(): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        return "$currentVolume / $maxVolume"
    }

    // ==================== 网络连接 ====================
    fun getWifiStatus(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            "已连接"
        } else {
            "未连接"
        }
    }

    fun getMobileDataStatus(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
            "已连接"
        } else {
            "未连接"
        }
    }

    fun getIpAddress(): String {
        try {
            // 简化实现，实际项目中应使用更可靠的方法
            return "查看网络设置"
        } catch (e: Exception) {
            return "未知"
        }
    }
}
