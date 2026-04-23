package com.tracemaster.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 轨迹分段实体类 - 用于大轨迹分片存储
 * 当轨迹点数量过多时，将轨迹分成多个段以提高查询性能
 */
@Entity(
    tableName = "track_segments",
    foreignKeys = [ForeignKey(
        entity = Track::class,
        parentColumns = ["id"],
        childColumns = ["trackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trackId")]
)
data class TrackSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val trackId: Long,                       // 所属轨迹ID
    val segmentIndex: Int,                   // 分段索引（从0开始）
    val startPointId: Long,                  // 起始点ID
    val endPointId: Long,                    // 结束点ID
    val pointCount: Int = 0,                 // 该段包含的点数
    val distance: Double = 0.0,              // 该段距离（米）
    val duration: Long = 0,                  // 该段时长（秒）
    val startTime: java.util.Date,           // 该段开始时间
    val endTime: java.util.Date,             // 该段结束时间
    val minAltitude: Double = 0.0,           // 最低海拔
    val maxAltitude: Double = 0.0,           // 最高海拔
    val avgSpeed: Float = 0f,                // 平均速度
    val maxSpeed: Float = 0f                 // 最大速度
) {
    /**
     * 获取分段名称
     */
    fun getSegmentName(): String {
        return "分段 ${segmentIndex + 1}"
    }
    
    /**
     * 格式化距离显示
     */
    fun getFormattedDistance(): String {
        return when {
            distance >= 1000 -> String.format("%.2f km", distance / 1000)
            else -> String.format("%.0f m", distance)
        }
    }
    
    /**
     * 格式化时长显示
     */
    fun getFormattedDuration(): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> String.format("%d秒", seconds)
        }
    }
}
