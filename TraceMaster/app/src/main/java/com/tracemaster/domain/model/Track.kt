package com.tracemaster.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 轨迹实体类 - 表示一条完整的轨迹记录
 */
@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,                          // 轨迹名称
    val startTime: Date,                       // 开始时间
    val endTime: Date?,                        // 结束时间（录制中为null）
    val totalDistance: Double = 0.0,          // 总距离（米）
    val totalTime: Long = 0,                   // 总时长（秒）
    val sportType: SportType = SportType.WALKING,  // 运动类型
    val pointCount: Int = 0,                   // 轨迹点数量
    val isFavorite: Boolean = false,           // 是否收藏
    val isArchived: Boolean = false,           // 是否归档
    val tags: String? = null,                  // 标签（JSON数组）
    val notes: String? = null,                 // 备注
    val filePath: String? = null,              // 导出文件路径
    val createTime: Date = Date(),             // 创建时间
    val updateTime: Date = Date()              // 更新时间
) {
    /**
     * 计算平均速度 (m/s)
     */
    fun getAverageSpeed(): Double {
        return if (totalTime > 0) totalDistance / totalTime else 0.0
    }

    /**
     * 计算最大速度 (需要在详情中单独计算)
     */
    fun getMaxSpeed(): Double {
        // 需要从track_points中查询最大速度
        return 0.0
    }

    /**
     * 格式化总时长显示
     */
    fun getFormattedTotalTime(): String {
        val hours = totalTime / 3600
        val minutes = (totalTime % 3600) / 60
        val seconds = totalTime % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%d:%02d", minutes, seconds)
            else -> String.format("%d秒", seconds)
        }
    }

    /**
     * 格式化总距离显示
     */
    fun getFormattedDistance(): String {
        return when {
            totalDistance >= 1000 -> String.format("%.2f km", totalDistance / 1000)
            else -> String.format("%.0f m", totalDistance)
        }
    }
}
