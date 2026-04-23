package com.tracemaster.util.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 高德地图管理器 - 封装高德地图SDK的核心功能
 */
@Singleton
class AMapManager @Inject constructor() {
    
    private val _mapStatus = MutableStateFlow(MapStatus.IDLE)
    val mapStatus: StateFlow<MapStatus> = _mapStatus
    
    private var currentTrackOverlay: Polyline? = null
    private var locationMarker: Marker? = null
    private val markers = mutableListOf<Marker>()
    
    /**
     * 初始化地图设置
     */
    fun setupMap(amap: AMap, context: Context) {
        amap.apply {
            // 启用定位图层
            isMyLocationEnabled = true
            // 设置定位模式
            myLocationStyle = MyLocationStyle().apply {
                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                radiusFillColor(Color.argb(50, 0, 100, 255))
                strokeColor(Color.argb(100, 0, 100, 255))
            }
            // 启用室内地图
            showIndoorMap(true)
            // 启用3D建筑
            showBuildings(true)
            // 设置地图类型
            mapType = AMap.MAP_TYPE_NORMAL
            // 启用缩放控制
            uiSettings.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isScaleControlsEnabled = true
                isRotateGesturesEnabled = true
                isTiltGesturesEnabled = true
                isScrollGesturesEnabled = true
                isZoomGesturesEnabled = true
            }
            // 设置最大和最小缩放级别
            setMinZoomLevel(3f)
            setMaxZoomLevel(20f)
        }
        
        Timber.d("AMap initialized successfully")
    }
    
    /**
     * 移动相机到指定位置
     */
    fun moveCamera(amap: AMap, latitude: Double, longitude: Double, zoom: Float = 15f) {
        try {
            val latLng = LatLng(latitude, longitude)
            amap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
            _mapStatus.value = MapStatus.MOVING
        } catch (e: Exception) {
            Timber.e(e, "Error moving camera")
            _mapStatus.value = MapStatus.ERROR
        }
    }
    
    /**
     * 移动相机到包含所有点的边界
     */
    fun moveCameraToBounds(amap: AMap, points: List<TrackPoint>, padding: Int = 100) {
        if (points.isEmpty()) return
        
        try {
            val boundsBuilder = LatLngBounds.Builder()
            points.forEach { point ->
                boundsBuilder.include(LatLng(point.latitude, point.longitude))
            }
            val bounds = boundsBuilder.build()
            amap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            _mapStatus.value = MapStatus.MOVING
        } catch (e: Exception) {
            Timber.e(e, "Error moving camera to bounds")
            _mapStatus.value = MapStatus.ERROR
        }
    }
    
    /**
     * 在地图上绘制轨迹
     */
    fun drawTrack(amap: AMap, trackPoints: List<TrackPoint>) {
        try {
            // 清除旧的轨迹
            currentTrackOverlay?.remove()
            
            if (trackPoints.isEmpty()) {
                currentTrackOverlay = null
                return
            }
            
            // 创建轨迹点列表
            val latLngPoints = trackPoints.map { point ->
                LatLng(point.latitude, point.longitude)
            }
            
            // 创建轨迹线
            currentTrackOverlay = amap.addPolyline(PolylineOptions().apply {
                addAll(latLngPoints)
                width(12f)
                color(Color.parseColor("#FF4081"))
                isGeodesic = true // 使用测地线
                setCustomTexture(null) // 可以使用自定义纹理
            })
            
            Timber.d("Track drawn with ${trackPoints.size} points")
        } catch (e: Exception) {
            Timber.e(e, "Error drawing track")
            _mapStatus.value = MapStatus.ERROR
        }
    }
    
    /**
     * 更新实时位置标记
     */
    fun updateLocationMarker(amap: AMap, latitude: Double, longitude: Double, bearing: Float = 0f) {
        try {
            val latLng = LatLng(latitude, longitude)
            
            if (locationMarker == null) {
                // 创建新标记
                locationMarker = amap.addMarker(MarkerOptions().apply {
                    position(latLng)
                    anchor(0.5f, 0.5f)
                    isDraggable = false
                    title("当前位置")
                })
            } else {
                // 更新现有标记
                locationMarker?.apply {
                    position = latLng
                    if (bearing != 0f) {
                        rotation = bearing
                    }
                }
            }
            
            // 平滑移动相机到当前位置
            amap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        } catch (e: Exception) {
            Timber.e(e, "Error updating location marker")
        }
    }
    
    /**
     * 添加标记到地图
     */
    fun addMarker(amap: AMap, latitude: Double, longitude: Double, title: String, snippet: String? = null): Marker? {
        return try {
            val marker = amap.addMarker(MarkerOptions().apply {
                position(LatLng(latitude, longitude))
                title(title)
                snippet(snippet)
                isDraggable = false
            })
            markers.add(marker)
            marker
        } catch (e: Exception) {
            Timber.e(e, "Error adding marker")
            null
        }
    }
    
    /**
     * 添加多个标记
     */
    fun addMarkers(amap: AMap, locations: List<Pair<Double, Double>>, titles: List<String>) {
        locations.forEachIndexed { index, (lat, lng) ->
            val title = if (index < titles.size) titles[index] else "标记${index + 1}"
            addMarker(amap, lat, lng, title)
        }
    }
    
    /**
     * 清除所有标记
     */
    fun clearMarkers(amap: AMap) {
        markers.forEach { it.remove() }
        markers.clear()
        locationMarker?.remove()
        locationMarker = null
    }
    
    /**
     * 清除轨迹
     */
    fun clearTrack() {
        currentTrackOverlay?.remove()
        currentTrackOverlay = null
    }
    
    /**
     * 清除所有覆盖物
     */
    fun clearAllOverlays(amap: AMap) {
        clearTrack()
        clearMarkers(amap)
        amap.clear()
    }
    
    /**
     * 获取地图截图
     */
    fun takeSnapshot(amap: AMap, callback: (Bitmap?) -> Unit) {
        try {
            amap.snapshot { bitmap ->
                callback(bitmap)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error taking snapshot")
            callback(null)
        }
    }
    
    /**
     * 销毁资源
     */
    fun onDestroy() {
        clearTrack()
        clearMarkersStub()
        _mapStatus.value = MapStatus.IDLE
    }
    
    private fun clearMarkersStub() {
        markers.forEach { it.remove() }
        markers.clear()
        locationMarker = null
    }
}

/**
 * 地图状态
 */
sealed class MapStatus {
    object IDLE : MapStatus()
    object MOVING : MapStatus()
    object LOADING : MapStatus()
    object ERROR : MapStatus()
}

/**
 * 扩展函数：将TrackPoint转换为LatLng
 */
fun TrackPoint.toLatLng(): LatLng = LatLng(this.latitude, this.longitude)

/**
 * 扩展函数：将TrackPoint列表转换为LatLng列表
 */
fun List<TrackPoint>.toLatLngList(): List<LatLng> = this.map { it.toLatLng() }
