package com.tracemaster.domain.model

/**
 * 运动类型枚举
 */
enum class SportType(val displayName: String) {
    WALKING("步行"),
    RUNNING("跑步"),
    CYCLING("骑行"),
    DRIVING("驾车"),
    HIKING("徒步"),
    OTHER("其他")
}

/**
 * 轨迹录制状态
 */
enum class RecordingStatus {
    IDLE,           // 未录制
    RECORDING,      // 录制中
    PAUSED,         // 已暂停
    FINISHED        // 已完成
}

/**
 * 定位点质量等级
 */
enum class PointQuality {
    HIGH,       // 高精度 (< 10m)
    MEDIUM,     // 中等精度 (10-50m)
    LOW,        // 低精度 (> 50m)
    INVALID     // 无效点 (漂移点)
}

/**
 * 导出格式枚举
 */
enum class ExportFormat(val extension: String, val displayName: String, val mimeType: String) {
    GPX("gpx", "GPX (.gpx)", "application/gpx+xml"),
    KML("kml", "KML (.kml)", "application/vnd.google-earth.kml+xml"),
    GEOJSON("geojson", "GeoJSON (.geojson)", "application/geo+json"),
    CSV("csv", "CSV (.csv)", "text/csv")
}
