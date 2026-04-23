package com.tracemaster.util.export

import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

/**
 * KML 格式导出器
 * KML (Keyhole Markup Language) 是 Google Earth 使用的地理数据格式
 */
object KmlExporter {
    
    private const val KML_HEADER = """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">"""
    
    private const val KML_FOOTER = "</kml>"
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    
    /**
     * 导出轨迹为 KML 格式
     * @param track 轨迹信息
     * @param points 轨迹点列表
     * @param writer 输出写入器
     */
    fun export(track: Track, points: List<TrackPoint>, writer: Writer) {
        try {
            writer.write(KML_HEADER)
            writer.write("\n")
            
            // 写入文档
            writer.write("  <Document>\n")
            writer.write("    <name>${escapeXml(track.name)}</name>\n")
            if (!track.notes.isNullOrBlank()) {
                writer.write("    <description>${escapeXml(track.notes)}</description>\n")
            }
            writer.write("    <Style id=\"trackStyle\">\n")
            writer.write("      <LineStyle>\n")
            writer.write("        <color>ff0000ff</color>\n")  // 蓝色（KML 使用 AABBGGRR 格式）
            writer.write("        <width>4</width>\n")
            writer.write("      </LineStyle>\n")
            writer.write("      <IconStyle>\n")
            writer.write("        <color>ff0000ff</color>\n")
            writer.write("        <scale>1.2</scale>\n")
            writer.write("      </IconStyle>\n")
            writer.write("    </Style>\n")
            
            // 写入轨迹
            writer.write("    <Placemark>\n")
            writer.write("      <name>${escapeXml(track.name)}</name>\n")
            writer.write("      <description>\n")
            writer.write("        <![CDATA[\n")
            writer.write("          <h3>${escapeXml(track.name)}</h3>\n")
            writer.write("          <p>类型：${track.sportType.displayName}</p>\n")
            writer.write("          <p>距离：${track.getFormattedDistance()}</p>\n")
            writer.write("          <p>时长：${track.getFormattedTotalTime()}</p>\n")
            writer.write("          <p>平均速度：${String.format("%.2f", track.getAverageSpeed() * 3.6)} km/h</p>\n")
            writer.write("          <p>开始时间：${dateFormat.format(track.startTime)}</p>\n")
            if (track.endTime != null) {
                writer.write("          <p>结束时间：${dateFormat.format(track.endTime)}</p>\n")
            }
            writer.write("          <p>点数：${track.pointCount}</p>\n")
            writer.write("        ]]>\n")
            writer.write("      </description>\n")
            writer.write("      <styleUrl>#trackStyle</styleUrl>\n")
            
            // 写入 LineString
            writer.write("      <LineString>\n")
            writer.write("        <tessellate>1</tessellate>\n")
            writer.write("        <altitudeMode>absolute</altitudeMode>\n")
            writer.write("        <coordinates>\n")
            
            // 写入坐标点
            for (point in points) {
                if (point.altitude > 0) {
                    writer.write("          ${point.longitude},${point.latitude},${point.altitude}\n")
                } else {
                    writer.write("          ${point.longitude},${point.latitude},0\n")
                }
            }
            
            writer.write("        </coordinates>\n")
            writer.write("      </LineString>\n")
            writer.write("    </Placemark>\n")
            
            // 写入起点标记
            if (points.isNotEmpty()) {
                val startPoint = points.first()
                writer.write("    <Placemark>\n")
                writer.write("      <name>起点</name>\n")
                writer.write("      <description>开始时间：${dateFormat.format(startPoint.timestamp)}</description>\n")
                writer.write("      <Point>\n")
                writer.write("        <coordinates>${startPoint.longitude},${startPoint.latitude},${startPoint.altitude}</coordinates>\n")
                writer.write("      </Point>\n")
                writer.write("    </Placemark>\n")
                
                // 写入终点标记
                val endPoint = points.last()
                writer.write("    <Placemark>\n")
                writer.write("      <name>终点</name>\n")
                writer.write("      <description>结束时间：${dateFormat.format(endPoint.timestamp)}</description>\n")
                writer.write("      <Point>\n")
                writer.write("        <coordinates>${endPoint.longitude},${endPoint.latitude},${endPoint.altitude}</coordinates>\n")
                writer.write("      </Point>\n")
                writer.write("    </Placemark>\n")
            }
            
            writer.write("  </Document>\n")
            writer.write(KML_FOOTER)
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
