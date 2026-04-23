package com.tracemaster.util.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * 定位数据类
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long,
    val provider: String,
    val isHighAccuracy: Boolean
) {
    fun toLocation(): Location {
        return Location(provider).apply {
            this@apply.latitude = this@LocationData.latitude
            this@apply.longitude = this@LocationData.longitude
            this@apply.altitude = this@LocationData.altitude
            this@apply.accuracy = this@LocationData.accuracy
            this@apply.speed = this@LocationData.speed
            this@apply.bearing = this@LocationData.bearing
            time = this@LocationData.timestamp
        }
    }
}

/**
 * 定位配置
 */
data class LocationConfig(
    val interval: Long = 2000L,              // 基础采集间隔 (ms)
    val fastestInterval: Long = 1000L,       // 最快采集间隔 (ms)
    val maxWaitTime: Long = 5000L,           // 最大等待时间
    val priority: Int = Priority.PRIORITY_HIGH_ACCURACY,
    val minDistance: Float = 5f              // 最小位移 (米)
)

/**
 * 定位管理器 - 封装 Google Play Services Location API
 * 
 * 功能：
 * - 高精度融合定位（GPS + 网络 + 传感器）
 * - 后台持续定位
 * - 运动状态识别
 * - 信号质量评估
 */
class LocationManager @Inject constructor(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var locationRequest: LocationRequest? = null
    private var currentConfig: LocationConfig = LocationConfig()

    /**
     * 检查位置权限
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查后台位置权限 (Android 10+)
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 创建定位请求配置
     */
    private fun buildLocationRequest(config: LocationConfig): LocationRequest {
        return LocationRequest.Builder(
            config.priority,
            config.interval
        ).apply {
            setMinUpdateIntervalMillis(config.fastestInterval)
            setMaxUpdateDelayMillis(config.maxWaitTime)
            setMinUpdateDistanceMeters(config.minDistance)
            setWaitForAccurateLocation(true)
        }.build()
    }

    /**
     * 获取实时位置流 - 用于前台实时展示
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(config: LocationConfig = LocationConfig()): Flow<LocationData> {
        return callbackFlow {
            if (!hasLocationPermission()) {
                close()
                return@callbackFlow
            }

            currentConfig = config
            locationRequest = buildLocationRequest(config)

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        trySend(location.toLocationData())
                    }
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        // 定位不可用，可以尝试切换到低功耗模式
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest!!,
                callback,
                Looper.getMainLooper()
            )

            awaitClose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }

    /**
     * 获取最后一次已知位置
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): LocationData? {
        return try {
            if (!hasLocationPermission()) {
                return null
            }
            fusedLocationClient.lastLocation.await()?.toLocationData()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 停止定位更新
     */
    fun stopLocationUpdates() {
        locationRequest = null
    }

    /**
     * Location 转 LocationData
     */
    private fun Location.toLocationData(): LocationData {
        return LocationData(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            accuracy = accuracy,
            speed = if (hasSpeed()) speed else 0f,
            bearing = if (hasBearing()) bearing else 0f,
            timestamp = time,
            provider = provider,
            isHighAccuracy = provider == LocationManager.GPS_PROVIDER && accuracy < 10
        )
    }

    companion object {
        const val GPS_PROVIDER = "gps"
        const val NETWORK_PROVIDER = "network"
        const val PASSIVE_PROVIDER = "passive"
    }
}

// 扩展函数：将 Task 转为 suspend 函数
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return kotlin.coroutines.suspendCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it, null) }
        addOnFailureListener { continuation.resumeWith(Result.failure(it)) }
    }
}
