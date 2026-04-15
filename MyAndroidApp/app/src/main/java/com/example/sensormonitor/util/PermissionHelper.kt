package com.example.sensormonitor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * Permission helper for managing runtime permissions
 */
class PermissionHelper {
    
    companion object {
        /**
         * Get all required permissions for the app
         */
        fun getRequiredPermissions(): Array<String> {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
            
            // Add body sensors permission for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
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
    }
}
