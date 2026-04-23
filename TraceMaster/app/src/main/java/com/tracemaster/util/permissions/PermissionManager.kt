package com.tracemaster.util.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 权限管理工具类
 * 处理运行时权限请求和状态检查
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 定位相关权限
    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // 存储相关权限（针对旧版本）
    val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    // 通知权限（Android 13+）
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    /**
     * 检查是否已授予所有位置权限
     */
    fun hasLocationPermission(): Boolean {
        return locationPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == 
                PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查是否已授予后台位置权限
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // 旧版本不需要单独申请后台权限
        }
    }

    /**
     * 检查是否已授予存储权限
     */
    fun hasStoragePermission(): Boolean {
        return storagePermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == 
                PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查是否已授予通知权限
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // 旧版本不需要通知权限
        }
    }

    /**
     * 检查是否需要显示权限解释对话框
     * 当用户之前拒绝过权限且选择了"不再询问"时返回true
     */
    fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
        return locationPermissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    /**
     * 请求位置权限
     * @param activity 当前Activity
     * @param launcher 权限请求结果回调
     */
    fun requestLocationPermission(
        activity: Activity,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        val permissionsToRequest = locationPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != 
                PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest)
        }
    }

    /**
     * 请求存储权限
     */
    fun requestStoragePermission(
        activity: Activity,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        val permissionsToRequest = storagePermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != 
                PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest)
        }
    }

    /**
     * 请求通知权限
     */
    fun requestNotificationPermission(
        activity: Activity,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != 
                PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(arrayOf(permission))
            }
        }
    }

    /**
     * 请求后台位置权限（需要在前台权限已授予的情况下）
     */
    fun requestBackgroundLocationPermission(
        activity: Activity,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBackgroundLocationPermission()) {
                launcher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            }
        }
    }

    /**
     * 获取权限拒绝说明文本
     */
    fun getPermissionDeniedMessage(deniedPermissions: List<String>): String {
        return when {
            deniedPermissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ->
                "后台位置权限被拒绝，无法在后台记录轨迹。请在系统设置中开启\"始终允许\"位置权限。"
            deniedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) ->
                "位置权限被拒绝，无法获取当前位置信息。请开启位置权限以使用轨迹录制功能。"
            deniedPermissions.contains(Manifest.permission.POST_NOTIFICATIONS) ->
                "通知权限被拒绝，无法显示录制状态通知。请开启通知权限。"
            else -> "部分必要权限被拒绝，应用功能可能受限。请在系统设置中开启相关权限。"
        }
    }
}
