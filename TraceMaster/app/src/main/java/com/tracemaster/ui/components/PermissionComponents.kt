package com.tracemaster.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tracemaster.util.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 权限请求对话框
 */
@Composable
fun PermissionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("去设置")
                    }
                }
            }
        }
    }
}

/**
 * 权限请求引导组件
 * 用于在需要权限时显示提示并请求权限
 */
@Composable
fun PermissionRequestHandler(
    permissionManager: PermissionManager,
    activity: Activity,
    requiredPermissions: List<String>,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (List<String>) -> Unit,
    content: @Composable () -> Unit
) {
    var showRationaleDialog by remember { mutableStateOf(false) }
    var deniedPermissions by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.filter { it.value }.keys.toList()
        val denied = permissions.filter { !it.value }.keys.toList()
        
        if (denied.isEmpty()) {
            onPermissionsGranted()
        } else {
            // 检查是否有永久拒绝的权限
            val shouldShowRationale = denied.any { permission ->
                activity.shouldShowRequestPermissionRationale(permission)
            }
            
            if (shouldShowRationale) {
                deniedPermissions = denied
                showRationaleDialog = true
            } else {
                onPermissionsDenied(denied)
            }
        }
    }
    
    // 首次加载时检查权限
    LaunchedEffect(Unit) {
        val missingPermissions = requiredPermissions.filter { permission ->
            activity.checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            onPermissionsGranted()
        } else {
            // 直接请求权限
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    if (showRationaleDialog) {
        PermissionDialog(
            title = "权限必要",
            message = permissionManager.getPermissionDeniedMessage(deniedPermissions),
            onConfirm = {
                showRationaleDialog = false
                activity.openAppSettings()
            },
            onDismiss = {
                showRationaleDialog = false
                onPermissionsDenied(deniedPermissions)
            }
        )
    }
    
    content()
}

/**
 * 简化的权限请求按钮
 */
@Composable
fun RequestPermissionButton(
    permissionManager: PermissionManager,
    activity: Activity,
    permissions: Array<String>,
    onRequestGranted: () -> Unit,
    onRequestDenied: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var lastDeniedPermissions by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val denied = permissions.filter { !it.value }.keys.toList()
        
        if (denied.isEmpty()) {
            onRequestGranted()
        } else {
            lastDeniedPermissions = denied
            val shouldShowRationale = denied.any { permission ->
                activity.shouldShowRequestPermissionRationale(permission)
            }
            
            if (shouldShowRationale) {
                showDialog = true
            } else {
                onRequestDenied(denied)
            }
        }
    }
    
    Button(
        onClick = {
            permissionLauncher.launch(permissions)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("授予权限")
    }
    
    if (showDialog) {
        PermissionDialog(
            title = "权限必要",
            message = permissionManager.getPermissionDeniedMessage(lastDeniedPermissions),
            onConfirm = {
                showDialog = false
                activity.openAppSettings()
            },
            onDismiss = {
                showDialog = false
                onRequestDenied(lastDeniedPermissions)
            }
        )
    }
}

/**
 * 打开应用设置页面
 */
fun Activity.openAppSettings() {
    val intent = android.content.Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", packageName, null)
    )
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
