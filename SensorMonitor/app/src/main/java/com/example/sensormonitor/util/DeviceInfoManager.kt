package com.example.sensormonitor.util

import android.content.Context
import android.os.Build
import java.io.File

class DeviceInfoManager(private val context: Context) {

    fun getDeviceModel(): String = Build.MODEL

    fun getAndroidVersion(): String {
        return when (Build.VERSION.SDK_INT) {
            36 -> "Android 16"
            35 -> "Android 15 (Vanilla Ice Cream)"
            34 -> "Android 14 (Upside Down Cake)"
            33 -> "Android 13 (Tiramisu)"
            32 -> "Android 12L (Snow Cone v2)"
            31 -> "Android 12 (Snow Cone)"
            30 -> "Android 11 (Red Velvet Cake)"
            29 -> "Android 10 (Quince Tart)"
            else -> "Unknown (${Build.VERSION.SDK_INT})"
        }
    }

    fun getSdkVersion(): Int = Build.VERSION.SDK_INT

    fun getManufacturer(): String = Build.MANUFACTURER

    fun getBrand(): String = Build.BRAND

    fun getScreenResolution(): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels} x ${metrics.heightPixels}"
    }

    fun getScreenDensity(): Int = context.resources.displayMetrics.densityDpi

    fun getTotalMemory(): String {
        val totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024)
        return "$totalMem MB"
    }

    fun getAvailableMemory(): String {
        val freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024)
        return "$freeMem MB"
    }

    fun getTotalStorage(): String {
        val statFs = android.os.StatFs(context.filesDir.absolutePath)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        return formatSize(totalBytes)
    }

    fun getAvailableStorage(): String {
        val statFs = android.os.StatFs(context.filesDir.absolutePath)
        val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        return formatSize(availableBytes)
    }

    fun getCpuCoreCount(): Int = Runtime.getRuntime().availableProcessors()

    fun getCpuArchitecture(): String {
        return if (Build.SUPPORTED_ABIS.isNotEmpty()) {
            Build.SUPPORTED_ABIS.joinToString(", ")
        } else {
            Build.CPU_ABI
        }
    }

    fun getBuildNumber(): String = Build.DISPLAY

    fun getSecurityPatch(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "N/A"
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
