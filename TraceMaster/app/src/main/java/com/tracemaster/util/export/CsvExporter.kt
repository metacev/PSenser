package com.tracemaster.util.export

import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 * CSV 格式导出器
 * 适用于导入到 Excel、Google Sheets 等表格软件
 */
object CsvExporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    
    // CSV 表头
    private const val HEADER = "index,latitude,longitude,altitude,speed,bearing,accuracy,timestamp,note"
    
    /**
     * 导出轨迹为 CSV 格式
     * @param track 轨迹信息
     * @param points 轨迹点列表
     * @param writer 输出写入器
     */
    fun export(track: Track, points: List<TrackPoint>, writer: Writer) {
        try {
            // 写入元数据注释
            writer.write("# Track: ${track.name}\n")
            writer.write("# Sport Type: ${track.sportType.displayName}\n")
            writer.write("# Total Distance: ${track.totalDistance} m\n")
            writer.write("# Total Time: ${track.totalTime} s\n")
            writer.write("# Point Count: ${track.pointCount}\n")
            writer.write("# Start Time: ${dateFormat.format(track.startTime)}\n")
            if (track.endTime != null) {
                writer.write("# End Time: ${dateFormat.format(track.endTime)}\n")
            }
            if (!track.notes.isNullOrBlank()) {
                writer.write("# Description: ${track.notes.replace("\n", " ")}\n")
            }
            writer.write("#\n")
            
            // 写入表头
            writer.write(HEADER)
            writer.write("\n")
            
            // 写入数据行
            for ((index, point) in points.withIndex()) {
                val row = buildString {
                    append(index + 1)
                    append(",")
                    append(point.latitude)
                    append(",")
                    append(point.longitude)
                    append(",")
                    append(if (point.altitude > 0) point.altitude else "")
                    append(",")
                    append(if (point.speed > 0) point.speed else "")
                    append(",")
                    append(if (point.bearing > 0) point.bearing else "")
                    append(",")
                    append(if (point.accuracy > 0) point.accuracy else "")
                    append(",")
                    append(dateFormat.format(point.timestamp))
                    append(",")
                    append(escapeCsv(point.note ?: ""))
                }
                writer.write(row)
                writer.write("\n")
            }
            
            writer.flush()
        } finally {
            writer.close()
        }
    }
    
    /**
     * 导出多条轨迹为 CSV 格式（合并到一个文件）
     */
    fun exportMultiple(tracks: List<Pair<Track, List<TrackPoint>>>, writer: Writer) {
        try {
            // 写入总元数据
            writer.write("# Exported from TraceMaster\n")
            writer.write("# Export Time: ${dateFormat.format(Date())}\n")
            writer.write("# Total Tracks: ${tracks.size}\n")
            writer.write("#\n")
            
            // 写入表头
            writer.write("track_name,track_type,$HEADER")
            writer.write("\n")
            
            // 写入每条轨迹的数据
            for ((track, points) in tracks) {
                for ((index, point) in points.withIndex()) {
                    val row = buildString {
                        append(escapeCsv(track.name))
                        append(",")
                        append(track.sportType.displayName)
                        append(",")
                        append(index + 1)
                        append(",")
                        append(point.latitude)
                        append(",")
                        append(point.longitude)
                        append(",")
                        append(if (point.altitude > 0) point.altitude else "")
                        append(",")
                        append(if (point.speed > 0) point.speed else "")
                        append(",")
                        append(if (point.bearing > 0) point.bearing else "")
                        append(",")
                        append(if (point.accuracy > 0) point.accuracy else "")
                        append(",")
                        append(dateFormat.format(point.timestamp))
                        append(",")
                        append(escapeCsv(point.note ?: ""))
                    }
                    writer.write(row)
                    writer.write("\n")
                }
                
                // 轨迹之间添加空行分隔
                writer.write("\n")
            }
            
            writer.flush()
        } finally {
            writer.close()
        }
    }
    
    /**
     * CSV 特殊字符转义
     */
    private fun escapeCsv(text: String): String {
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\""
        }
        return text
    }
}
