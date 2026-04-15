package com.example.sensormonitor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * Permission helper for managing runtime permissions
 * Handles permission differences across Android 10-15
 */
class PermissionHelper {

    companion object {
        /**
         * Get all required permissions for the app
         * Adapts to different Android versions
         */
        fun getRequiredPermissions(): Array<String> {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )

            // Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }

            // Android 13+ (API 33+) - New media permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }

            return permissions.toTypedArray()
        }

        /**
         * Check if all required permissions are granted
         */
        fun hasAllPermissions(context: android.content.Context): Boolean {
            return getRequiredPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * Request permissions using the provided launcher
         */
        fun requestPermissions(launcher: ActivityResultLauncher<Array<String>>) {
            launcher.launch(getRequiredPermissions())
        }
        
        /**
         * Get Android version name for display
         */
        fun getAndroidVersionName(sdkInt: Int): String {
            return when {
                sdkInt >= 35 -> "Android 15 (Vanilla Ice Cream)"
                sdkInt >= 34 -> "Android 14 (Upside Down Cake)"
                sdkInt >= 33 -> "Android 13 (Tiramisu)"
                sdkInt >= 32 -> "Android 12L (Snow Cone v2)"
                sdkInt >= 31 -> "Android 12 (Snow Cone)"
                sdkInt >= 30 -> "Android 11 (Red Velvet Cake)"
                sdkInt >= 29 -> "Android 10 (Quince Tart)"
                else -> "Unknown Android Version"
            }
        }
    }
}
