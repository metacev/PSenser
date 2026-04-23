package com.tracemaster.util.export

import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import org.json.JSONArray
import org.json.JSONObject
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 * GeoJSON 格式导出器
 * GeoJSON 是一种基于 JSON 的地理数据交换格式
 */
object GeoJsonExporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    
    /**
     * 导出轨迹为 GeoJSON 格式
     * @param track 轨迹信息
     * @param points 轨迹点列表
     * @param writer 输出写入器
     */
    fun export(track: Track, points: List<TrackPoint>, writer: Writer) {
        try {
            val geoJson = JSONObject()
            geoJson.put("type", "FeatureCollection")
            
            // 元数据
            val metadata = JSONObject()
            metadata.put("name", track.name)
            if (!track.notes.isNullOrBlank()) {
                metadata.put("description", track.notes)
            }
            metadata.put("sportType", track.sportType.displayName)
            metadata.put("totalDistance", track.totalDistance)
            metadata.put("totalTime", track.totalTime)
            metadata.put("pointCount", track.pointCount)
            metadata.put("startTime", dateFormat.format(track.startTime))
            if (track.endTime != null) {
                metadata.put("endTime", dateFormat.format(track.endTime))
            }
            geoJson.put("metadata", metadata)
            
            // 创建 Feature
            val features = JSONArray()
            
            // 轨迹线 Feature
            val lineFeature = JSONObject()
            lineFeature.put("type", "Feature")
            lineFeature.put("properties", createProperties(track, "LineString"))
            
            // 创建 LineString 几何
            val lineGeometry = JSONObject()
            lineGeometry.put("type", "LineString")
            val coordinates = JSONArray()
            for (point in points) {
                val coord = JSONArray()
                coord.put(point.longitude)
                coord.put(point.latitude)
                if (point.altitude > 0) {
                    coord.put(point.altitude)
                }
                coordinates.put(coord)
            }
            lineGeometry.put("coordinates", coordinates)
            lineFeature.put("geometry", lineGeometry)
            features.put(lineFeature)
            
            // 起点 Feature
            if (points.isNotEmpty()) {
                val startPoint = points.first()
                val startFeature = JSONObject()
                startFeature.put("type", "Feature")
                
                val startProps = JSONObject()
                startProps.put("type", "start")
                startProps.put("name", "起点")
                startProps.put("timestamp", dateFormat.format(startPoint.timestamp))
                startFeature.put("properties", startProps)
                
                val startGeometry = JSONObject()
                startGeometry.put("type", "Point")
                val startCoord = JSONArray()
                startCoord.put(startPoint.longitude)
                startCoord.put(startPoint.latitude)
                if (startPoint.altitude > 0) {
                    startCoord.put(startPoint.altitude)
                }
                startGeometry.put("coordinates", startCoord)
                startFeature.put("geometry", startGeometry)
                
                features.put(startFeature)
                
                // 终点 Feature
                val endPoint = points.last()
                val endFeature = JSONObject()
                endFeature.put("type", "Feature")
                
                val endProps = JSONObject()
                endProps.put("type", "end")
                endProps.put("name", "终点")
                endProps.put("timestamp", dateFormat.format(endPoint.timestamp))
                endFeature.put("properties", endProps)
                
                val endGeometry = JSONObject()
                endGeometry.put("type", "Point")
                val endCoord = JSONArray()
                endCoord.put(endPoint.longitude)
                endCoord.put(endPoint.latitude)
                if (endPoint.altitude > 0) {
                    endCoord.put(endPoint.altitude)
                }
                endGeometry.put("coordinates", endCoord)
                endFeature.put("geometry", endGeometry)
                
                features.put(endFeature)
            }
            
            geoJson.put("features", features)
            
            // 写入输出
            writer.write(geoJson.toString(2))  // 2 空格缩进
            writer.flush()
        } finally {
            writer.close()
        }
    }
    
    /**
     * 创建属性对象
     */
    private fun createProperties(track: Track, geometryType: String): JSONObject {
        val props = JSONObject()
        props.put("name", track.name)
        props.put("geometryType", geometryType)
        props.put("sportType", track.sportType.displayName)
        props.put("distance", track.totalDistance)
        props.put("duration", track.totalTime)
        props.put("avgSpeed", track.getAverageSpeed())
        if (!track.notes.isNullOrBlank()) {
            props.put("description", track.notes)
        }
        return props
    }
}
