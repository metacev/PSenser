package com.tracemaster.util.export

import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 * GPX 格式导出器
 * GPX (GPS Exchange Format) 是一种标准的 GPS 数据交换格式
 */
object GpxExporter {
    
    private const val GPX_HEADER = """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="TraceMaster" xmlns="http://www.topografix.com/GPX/1/1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">"""
    
    private const val GPX_FOOTER = "</gpx>"
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    /**
     * 导出轨迹为 GPX 格式
     * @param track 轨迹信息
     * @param points 轨迹点列表
     * @param writer 输出写入器
     */
    fun export(track: Track, points: List<TrackPoint>, writer: Writer) {
        try {
            writer.write(GPX_HEADER)
            writer.write("\n")
            
            // 写入轨迹元数据
            writer.write("  <metadata>\n")
            writer.write("    <name>${escapeXml(track.name)}</name>\n")
            writer.write("    <desc>${escapeXml(track.notes ?: "")}</desc>\n")
            writer.write("    <time>${dateFormat.format(track.startTime)}</time>\n")
            if (track.endTime != null) {
                writer.write("    <endtime>${dateFormat.format(track.endTime)}</endtime>\n")
            }
            writer.write("  </metadata>\n")
            
            // 写入轨迹段
            writer.write("  <trk>\n")
            writer.write("    <name>${escapeXml(track.name)}</name>\n")
            writer.write("    <type>${track.sportType.displayName}</type>\n")
            writer.write("    <trkseg>\n")
            
            // 写入每个轨迹点
            for (point in points) {
                writer.write("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
                
                if (point.altitude > 0) {
                    writer.write("        <ele>${point.altitude}</ele>\n")
                }
                
                writer.write("        <time>${dateFormat.format(point.timestamp)}</time>\n")
                
                if (point.speed > 0) {
                    writer.write("        <speed>${point.speed}</speed>\n")
                }
                
                if (point.bearing > 0) {
                    writer.write("        <course>${point.bearing}</course>\n")
                }
                
                if (point.accuracy > 0) {
                    writer.write("        <hdop>${point.accuracy / 10.0}</hdop>\n")
                }
                
                if (!point.note.isNullOrBlank()) {
                    writer.write("        <cmt>${escapeXml(point.note)}</cmt>\n")
                }
                
                writer.write("      </trkpt>\n")
            }
            
            writer.write("    </trkseg>\n")
            writer.write("  </trk>\n")
            
            writer.write(GPX_FOOTER)
            writer.flush()
        } finally {
            writer.close()
        }
    }
    
    /**
     * 导出多条轨迹为 GPX 格式
     */
    fun exportMultiple(tracks: List<Pair<Track, List<TrackPoint>>>, writer: Writer) {
        try {
            writer.write(GPX_HEADER)
            writer.write("\n")
            
            for ((track, points) in tracks) {
                writer.write("  <trk>\n")
                writer.write("    <name>${escapeXml(track.name)}</name>\n")
                if (!track.notes.isNullOrBlank()) {
                    writer.write("    <desc>${escapeXml(track.notes)}</desc>\n")
                }
                writer.write("    <type>${track.sportType.displayName}</type>\n")
                writer.write("    <trkseg>\n")
                
                for (point in points) {
                    writer.write("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
                    if (point.altitude > 0) {
                        writer.write("        <ele>${point.altitude}</ele>\n")
                    }
                    writer.write("        <time>${dateFormat.format(point.timestamp)}</time>\n")
                    if (point.speed > 0) {
                        writer.write("        <speed>${point.speed}</speed>\n")
                    }
                    writer.write("      </trkpt>\n")
                }
                
                writer.write("    </trkseg>\n")
                writer.write("  </trk>\n")
            }
            
            writer.write(GPX_FOOTER)
            writer.flush()
        } finally {
            writer.close()
        }
    }
    
    /**
     * XML 特殊字符转义
     */
    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
