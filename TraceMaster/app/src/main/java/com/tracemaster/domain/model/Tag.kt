package com.tracemaster.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 标签实体类 - 用于轨迹分类和搜索
 */
@Entity(
    tableName = "tags",
    indices = [Index("name", unique = true)]
)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,                        // 标签名称
    val color: Int = 0xFF2196F3.toInt(),     // 标签颜色（ARGB）
    val icon: String? = null,                // 图标名称
    val usageCount: Int = 0,                 // 使用次数
    val createTime: Date = Date(),           // 创建时间
    val updateTime: Date = Date()            // 更新时间
) {
    companion object {
        // 预设标签颜色
        val PRESET_COLORS = listOf(
            0xFFFF5252.toInt(),  // 红色
            0xFFFF9800.toInt(),  // 橙色
            0xFFFFC107.toInt(),  // 黄色
            0xFF4CAF50.toInt(),  // 绿色
            0xFF00BCD4.toInt(),  // 青色
            0xFF2196F3.toInt(),  // 蓝色
            0xFF3F51B5.toInt(),  // 靛蓝
            0xFF9C27B0.toInt(),  // 紫色
            0xFFE91E63.toInt(),  // 粉红
            0xFF795548.toInt()   // 棕色
        )
    }
    
    /**
     * 获取颜色字符串表示（#RRGGBB）
     */
    fun getColorString(): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }
}

/**
 * 轨迹与标签的关联表
 */
@Entity(
    tableName = "track_tags",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId"), Index("tagId")]
)
data class TrackTagCrossRef(
    val trackId: Long,
    val tagId: Long
)
