package com.tracemaster.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 轨迹照片实体类 - 记录轨迹上拍摄的照片信息
 */
@Entity(
    tableName = "track_photos",
    foreignKeys = [ForeignKey(
        entity = Track::class,
        parentColumns = ["id"],
        childColumns = ["trackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trackId"), Index("pointId")]
)
data class TrackPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val trackId: Long,                       // 所属轨迹ID
    val pointId: Long?,                      // 关联的轨迹点ID（可选）
    val filePath: String,                    // 照片文件路径
    val thumbnailPath: String?,              // 缩略图路径
    val latitude: Double,                    // 拍摄位置纬度
    val longitude: Double,                   // 拍摄位置经度
    val altitude: Double = 0.0,              // 拍摄位置海拔
    val timestamp: Date,                     // 拍摄时间
    val caption: String? = null,             // 照片说明
    val isCover: Boolean = false,            // 是否为封面照片
    val fileSize: Long = 0,                  // 文件大小（字节）
    val createTime: Date = Date()            // 创建时间
) {
    /**
     * 获取坐标字符串
     */
    fun getCoordinateString(): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }
    
    /**
     * 获取格式化文件大小
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> String.format("%.2f MB", fileSize / (1024.0 * 1024.0))
        }
    }
}
