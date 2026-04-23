package com.tracemaster.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 轨迹点实体类 - 表示轨迹中的单个定位点
 */
@Entity(
    tableName = "track_points",
    foreignKeys = [ForeignKey(
        entity = Track::class,
        parentColumns = ["id"],
        childColumns = ["trackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trackId"), Index("segmentIndex")]
)
data class TrackPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val trackId: Long,                       // 所属轨迹ID
    val latitude: Double,                    // 纬度
    val longitude: Double,                   // 经度
    val altitude: Double = 0.0,              // 海拔（米）
    val accuracy: Float = 0f,                // 精度半径（米）
    val speed: Float = 0f,                   // 速度（m/s）
    val bearing: Float = 0f,                 // 方向角（度，0-360）
    val timestamp: Date,                     // 时间戳
    val segmentIndex: Int = 0,               // 分段索引（用于大轨迹分片）
    val pointIndex: Int = 0,                 // 点在轨迹中的序号
    val isLowQuality: Boolean = false,       // 是否低质量点（精度>50m）
    val isDrift: Boolean = false,            // 是否漂移点
    val note: String? = null,                // 备注
    val photoPath: String? = null            // 关联照片路径
) {
    /**
     * 获取点质量等级
     */
    fun getQuality(): PointQuality {
        return when {
            isDrift -> PointQuality.INVALID
            accuracy < 10 -> PointQuality.HIGH
            accuracy < 50 -> PointQuality.MEDIUM
            else -> PointQuality.LOW
        }
    }

    /**
     * 坐标字符串表示
     */
    fun getCoordinateString(): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }

    /**
     * 格式化速度显示
     */
    fun getFormattedSpeed(): String {
        val kmh = speed * 3.6  // m/s 转 km/h
        return String.format("%.1f km/h", kmh)
    }
}
