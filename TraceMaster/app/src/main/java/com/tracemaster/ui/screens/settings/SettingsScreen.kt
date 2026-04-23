package com.tracemaster.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var distanceUnit by remember { mutableStateOf("km") }
    var autoPause by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 外观设置
            SettingsSection(title = "外观") {
                SettingsItem(
                    title = "深色模式",
                    subtitle = "跟随系统或手动切换",
                    trailing = {
                        Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
                    }
                )
            }
            
            // 单位设置
            SettingsSection(title = "单位") {
                SettingsItem(
                    title = "距离单位",
                    subtitle = if (distanceUnit == "km") "公里 (km)" else "英里 (mi)",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(distanceUnit, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    },
                    onClick = { /* TODO: 选择单位 */ }
                )
            }
            
            // 录制设置
            SettingsSection(title = "录制") {
                SettingsItem(
                    title = "自动暂停",
                    subtitle = "静止时自动暂停录制",
                    trailing = {
                        Switch(checked = autoPause, onCheckedChange = { autoPause = it })
                    }
                )
                
                SettingsItem(
                    title = "定位精度",
                    subtitle = "高精度（更耗电）",
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    onClick = { /* TODO: 选择精度 */ }
                )
                
                SettingsItem(
                    title = "采集频率",
                    subtitle = "每 2 秒或 5 米",
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    onClick = { /* TODO: 设置频率 */ }
                )
            }
            
            // 数据管理
            SettingsSection(title = "数据") {
                SettingsItem(
                    title = "导出数据",
                    subtitle = "导出所有轨迹为 GPX/KML",
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    onClick = { /* TODO: 导出 */ }
                )
                
                SettingsItem(
                    title = "清除缓存",
                    subtitle = "清理地图缓存数据",
                    trailing = {
                        Text("12 MB", style = MaterialTheme.typography.bodySmall)
                    },
                    onClick = { /* TODO: 清除缓存 */ }
                )
            }
            
            // 关于
            SettingsSection(title = "关于") {
                SettingsItem(
                    title = "版本",
                    subtitle = "1.0.0",
                    trailing = null
                )
                
                SettingsItem(
                    title = "隐私政策",
                    subtitle = "查看隐私政策",
                    trailing = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    onClick = { /* TODO: 打开隐私政策 */ }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(content = content)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
