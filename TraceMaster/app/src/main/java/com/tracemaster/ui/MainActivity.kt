package com.tracemaster.ui

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.tracemaster.ui.theme.TraceMasterTheme
import com.tracemaster.ui.navigation.AppNavigation
import com.tracemaster.util.permissions.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    private var hasRequiredPermissions by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 注册权限请求启动器
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            checkPermissions()
        }
        
        setContent {
            TraceMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 检查并请求必要权限
                    LaunchedEffect(Unit) {
                        if (!hasRequiredPermissions) {
                            requestNecessaryPermissions(permissionLauncher)
                        }
                    }
                    
                    if (hasRequiredPermissions) {
                        AppNavigation()
                    } else {
                        // 可以在这里显示权限请求界面
                        // 暂时直接显示导航，权限会在需要时请求
                        AppNavigation()
                    }
                }
            }
        }
    }
    
    private fun checkPermissions() {
        hasRequiredPermissions = permissionManager.hasLocationPermission() &&
                permissionManager.hasNotificationPermission()
    }
    
    private fun requestNecessaryPermissions(
        launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    ) {
        val requiredPermissions = mutableListOf<String>()
        
        // 添加位置权限
        requiredPermissions.addAll(
            permissionManager.locationPermissions.filter { permission ->
                ContextCompat.checkSelfPermission(this, permission) != 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
        
        // 添加通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionManager.hasNotificationPermission()) {
                requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (requiredPermissions.isNotEmpty()) {
            launcher.launch(requiredPermissions.toTypedArray())
        } else {
            hasRequiredPermissions = true
        }
    }
}
