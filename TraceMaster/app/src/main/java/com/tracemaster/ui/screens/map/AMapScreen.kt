package com.tracemaster.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.tracemaster.util.map.AMapManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * 高德地图界面组件
 * 使用原生 MapView 集成高德地图 SDK
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AMapScreen(
    onNavigateBack: () -> Unit,
    onStartRecording: () -> Unit,
    viewModel: AMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // 权限请求
    val locationPermissionRequired = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 处理权限结果
    }
    
    // MapView 状态
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    
    // 收集地图状态
    val mapStatus by viewModel.mapStatus.collectAsState()
    
    // 初始化地图
    LaunchedEffect(Unit) {
        if (locationPermissionRequired) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // 监听位置更新
    LaunchedEffect(viewModel) {
        viewModel.locationFlow.collectLatest { location ->
            location?.let { loc ->
                aMap?.let { map ->
                    viewModel.aMapManager.updateLocationMarker(
                        map,
                        loc.latitude,
                        loc.longitude,
                        loc.bearing
                    )
                }
            }
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            viewModel.aMapManager.onDestroy()
            mapView?.onDestroy()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("地图") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartRecording,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("开始", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidMapView(
                modifier = Modifier.fillMaxSize(),
                onMapCreated = { createdAMap, createdMapView ->
                    aMap = createdAMap
                    mapView = createdMapView
                    viewModel.aMapManager.setupMap(createdAMap, context)
                    viewModel.aMapManager.moveCamera(createdAMap, 39.9042, 116.4074, 15f)
                }
            )
            
            // 定位按钮
            IconButton(
                onClick = {
                    viewModel.getCurrentLocation()?.let { loc ->
                        aMap?.let { map ->
                            viewModel.aMapManager.moveCamera(map, loc.latitude, loc.longitude, 17f)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "我的位置",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            if (mapStatus is com.tracemaster.util.map.MapStatus.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 用于在 Compose 中嵌入原生 MapView 的包装器
 */
@Composable
fun AndroidMapView(
    modifier: Modifier = Modifier,
    onMapCreated: (AMap, MapView) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    
    AndroidView(
        factory = { ctx ->
            mapView.apply {
                onCreate(Bundle())
                getMapAsync { amap ->
                    onMapCreated(amap, this)
                }
                onResume()
            }
        },
        modifier = modifier,
        onRelease = {
            mapView.onPause()
            mapView.onDestroy()
        }
    )
}

/**
 * 高德地图 ViewModel
 */
@HiltViewModel
class AMapViewModel @Inject constructor(
    val aMapManager: AMapManager
) : ViewModel() {
    
    val mapStatus = aMapManager.mapStatus
    val locationFlow = MutableStateFlow<android.location.Location?>(null)
    
    fun getCurrentLocation(): android.location.Location? = locationFlow.value
}
