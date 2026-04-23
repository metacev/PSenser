package com.tracemaster.util.importer

import android.content.Context
import android.net.Uri
import com.tracemaster.domain.model.SportType
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.abs

/**
 * GPX 格式导入器
 */
object GpxImporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    
    /**
     * 从 GPX 文件导入轨迹
     */
    fun import(context: Context, uri: Uri): List<Pair<Track, List<TrackPoint>>> {
        val tracks = mutableListOf<Pair<Track, List<TrackPoint>>>()
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(inputStream)
            doc.documentElement.normalize()
            
            // 查找所有 trk 元素
            val trackList = doc.getElementsByTagName("trk")
            
            for (i in 0 until trackList.length) {
                val trk = trackList.item(i)
                val trkElement = trk as org.w3c.dom.Element
                
                // 解析轨迹元数据
                val name = getElementText(trkElement, "name") ?: "未命名轨迹"
                val desc = getElementText(trkElement, "desc")
                val type = getElementText(trkElement, "type")
                val sportType = parseSportType(type)
                
                // 解析轨迹段
                val allPoints = mutableListOf<TrackPoint>()
                val segList = trkElement.getElementsByTagName("trkseg")
                
                for (j in 0 until segList.length) {
                    val seg = segList.item(j)
                    val segElement = seg as org.w3c.dom.Element
                    val points = segElement.getElementsByTagName("trkpt")
                    
                    for (k in 0 until points.length) {
                        val point = points.item(k)
                        val pointElement = point as org.w3c.dom.Element
                        
                        val lat = pointElement.getAttribute("lat").toDoubleOrNull() ?: continue
                        val lon = pointElement.getAttribute("lon").toDoubleOrNull() ?: continue
                        
                        val ele = getElementText(pointElement, "ele")?.toDoubleOrNull() ?: 0.0
                        val timeStr = getElementText(pointElement, "time")
                        val timestamp = parseTime(timeStr) ?: Date()
                        
                        val speed = getElementText(pointElement, "speed")?.toFloatOrNull() ?: 0f
                        val course = getElementText(pointElement, "course")?.toFloatOrNull() ?: 0f
                        val hdop = getElementText(pointElement, "hdop")?.toFloatOrNull() ?: 0f
                        
                        val trackPoint = TrackPoint(
                            latitude = lat,
                            longitude = lon,
                            altitude = ele,
                            accuracy = hdop * 10,  // HDOP 转换为米
                            speed = speed,
                            bearing = course,
                            timestamp = timestamp,
                            pointIndex = allPoints.size
                        )
                        
                        allPoints.add(trackPoint)
                    }
                }
                
                // 计算统计数据
                val startTime = allPoints.firstOrNull()?.timestamp ?: Date()
                val endTime = allPoints.lastOrNull()?.timestamp ?: Date()
                val totalTime = ((endTime.time - startTime.time) / 1000).coerceAtLeast(0)
                
                var totalDistance = 0.0
                for (i in 1 until allPoints.size) {
                    val prev = allPoints[i - 1]
                    val curr = allPoints[i]
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        prev.latitude, prev.longitude,
                        curr.latitude, curr.longitude,
                        results
                    )
                    totalDistance += results[0].toDouble()
                }
                
                val track = Track(
                    name = name,
                    startTime = startTime,
                    endTime = endTime,
                    totalDistance = totalDistance,
                    totalTime = totalTime,
                    sportType = sportType,
                    pointCount = allPoints.size,
                    notes = desc
                )
                
                tracks.add(Pair(track, allPoints))
            }
        }
        
        return tracks
    }
    
    private fun getElementText(parent: org.w3c.dom.Element, tagName: String): String? {
        val elements = parent.getElementsByTagName(tagName)
        return if (elements.length > 0) elements.item(0).textContent else null
    }
    
    private fun parseTime(timeStr: String?): Date? {
        return timeStr?.let {
            try {
                dateFormat.parse(it.replace("Z", "+0000"))
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun parseSportType(type: String?): SportType {
        return when (type?.lowercase()) {
            "walking", "hiking" -> SportType.WALKING
            "running" -> SportType.RUNNING
            "cycling", "biking" -> SportType.CYCLING
            "driving" -> SportType.DRIVING
            else -> SportType.OTHER
        }
    }
}

/**
 * CSV 格式导入器
 */
object CsvImporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    
    /**
     * 从 CSV 文件导入轨迹
     */
    fun import(context: Context, uri: Uri): Pair<Track, List<TrackPoint>>? {
        val points = mutableListOf<TrackPoint>()
        var trackName = "导入的轨迹"
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                var headerParsed = false
                var columnIndex = mapOf<String, Int>()
                
                while (reader.readLine().also { line = it } != null) {
                    line = line!!.trim()
                    
                    // 跳过注释
                    if (line.startsWith("#")) {
                        if (line.startsWith("# Track:")) {
                            trackName = line.substringAfter("# Track:").trim()
                        }
                        continue
                    }
                    
                    // 解析表头
                    if (!headerParsed) {
                        val headers = line.split(",")
                        columnIndex = headers.mapIndexed { index, header ->
                            header.trim() to index
                        }.toMap()
                        headerParsed = true
                        continue
                    }
                    
                    // 解析数据行
                    val values = line.split(",")
                    try {
                        val lat = values.getOrNull(columnIndex["latitude"] ?: -1)?.toDoubleOrNull() ?: continue
                        val lon = values.getOrNull(columnIndex["longitude"] ?: -1)?.toDoubleOrNull() ?: continue
                        
                        val alt = values.getOrNull(columnIndex["altitude"] ?: -1)?.toDoubleOrNull() ?: 0.0
                        val speed = values.getOrNull(columnIndex["speed"] ?: -1)?.toFloatOrNull() ?: 0f
                        val bearing = values.getOrNull(columnIndex["bearing"] ?: -1)?.toFloatOrNull() ?: 0f
                        val accuracy = values.getOrNull(columnIndex["accuracy"] ?: -1)?.toFloatOrNull() ?: 0f
                        val timeStr = values.getOrNull(columnIndex["timestamp"] ?: -1)
                        val timestamp = timeStr?.let { dateFormat.parse(it) } ?: Date()
                        
                        val trackPoint = TrackPoint(
                            latitude = lat,
                            longitude = lon,
                            altitude = alt,
                            accuracy = accuracy,
                            speed = speed,
                            bearing = bearing,
                            timestamp = timestamp,
                            pointIndex = points.size
                        )
                        
                        points.add(trackPoint)
                    } catch (e: Exception) {
                        // 跳过无效行
                        continue
                    }
                }
            }
        }
        
        if (points.isEmpty()) return null
        
        // 计算统计数据
        val startTime = points.firstOrNull()?.timestamp ?: Date()
        val endTime = points.lastOrNull()?.timestamp ?: Date()
        val totalTime = ((endTime.time - startTime.time) / 1000).coerceAtLeast(0)
        
        var totalDistance = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                prev.latitude, prev.longitude,
                curr.latitude, curr.longitude,
                results
            )
            totalDistance += results[0].toDouble()
        }
        
        val track = Track(
            name = trackName,
            startTime = startTime,
            endTime = endTime,
            totalDistance = totalDistance,
            totalTime = totalTime,
            sportType = SportType.OTHER,
            pointCount = points.size
        )
        
        return Pair(track, points)
    }
}

/**
 * GeoJSON 格式导入器
 */
object GeoJsonImporter {
    
    /**
     * 从 GeoJSON 文件导入轨迹
     */
    fun import(context: Context, uri: Uri): List<Pair<Track, List<TrackPoint>>> {
        val tracks = mutableListOf<Pair<Track, List<TrackPoint>>>()
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val content = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(content)
            
            val features = jsonObject.optJSONArray("features") ?: return@use
            
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.getJSONObject("geometry")
                val properties = feature.optJSONObject("properties") ?: JSONObject()
                
                val geometryType = geometry.getString("type")
                
                if (geometryType == "LineString") {
                    val coordinates = geometry.getJSONArray("coordinates")
                    val points = parseCoordinates(coordinates)
                    
                    if (points.isNotEmpty()) {
                        val track = createTrackFromProperties(properties, points)
                        tracks.add(Pair(track, points))
                    }
                }
            }
        }
        
        return tracks
    }
    
    private fun parseCoordinates(coordinates: JSONArray): List<TrackPoint> {
        val points = mutableListOf<TrackPoint>()
        
        for (i in 0 until coordinates.length()) {
            val coord = coordinates.getJSONArray(i)
            val lon = coord.getDouble(0)
            val lat = coord.getDouble(1)
            val alt = if (coord.length() > 2) coord.getDouble(2) else 0.0
            
            val trackPoint = TrackPoint(
                latitude = lat,
                longitude = lon,
                altitude = alt,
                timestamp = Date(),
                pointIndex = i
            )
            
            points.add(trackPoint)
        }
        
        return points
    }
    
    private fun createTrackFromProperties(properties: JSONObject, points: List<TrackPoint>): Track {
        val name = properties.optString("name", "导入的轨迹")
        val distance = properties.optDouble("distance", 0.0)
        val duration = properties.optLong("duration", 0)
        val sportTypeStr = properties.optString("sportType", "")
        
        val sportType = when (sportTypeStr.lowercase()) {
            "步行", "walking" -> SportType.WALKING
            "跑步", "running" -> SportType.RUNNING
            "骑行", "cycling" -> SportType.CYCLING
            "驾车", "driving" -> SportType.DRIVING
            else -> SportType.OTHER
        }
        
        return Track(
            name = name,
            startTime = points.firstOrNull()?.timestamp ?: Date(),
            endTime = points.lastOrNull()?.timestamp ?: Date(),
            totalDistance = distance,
            totalTime = duration,
            sportType = sportType,
            pointCount = points.size
        )
    }
}
