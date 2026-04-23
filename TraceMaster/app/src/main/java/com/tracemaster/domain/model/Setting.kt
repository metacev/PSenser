package com.tracemaster.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 设置实体类 - 存储用户应用设置
 */
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey
    val key: String,                         // 设置键
    val value: String,                       // 设置值
    val type: SettingType = SettingType.STRING,  // 设置类型
    val description: String? = null,         // 设置描述
    val updateTime: Date = Date()            // 更新时间
) {
    /**
     * 获取整数值
     */
    fun getIntValue(): Int {
        return try {
            value.toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取布尔值
     */
    fun getBooleanValue(): Boolean {
        return value.equals("true", ignoreCase = true)
    }
    
    /**
     * 获取浮点数值
     */
    fun getFloatValue(): Float {
        return try {
            value.toFloat()
        } catch (e: Exception) {
            0f
        }
    }
    
    companion object {
        // 常用设置键
        const val KEY_RECORDING_INTERVAL = "recording_interval"          // 记录间隔（秒）
        const val KEY_MIN_DISTANCE_FILTER = "min_distance_filter"        // 最小距离过滤（米）
        const val KEY_ACCURACY_THRESHOLD = "accuracy_threshold"          // 精度阈值（米）
        const val KEY_AUTO_PAUSE_ENABLED = "auto_pause_enabled"          // 自动暂停启用
        const val KEY_AUTO_PAUSE_SPEED = "auto_pause_speed"              // 自动暂停速度阈值
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"                  // 保持屏幕常亮
        const val KEY_VOICE_FEEDBACK = "voice_feedback"                  // 语音反馈
        const val KEY_NOTIFICATION_STYLE = "notification_style"          // 通知样式
        const val KEY_MAP_PROVIDER = "map_provider"                      // 地图提供商
        const val KEY_MAP_SATELLITE = "map_satellite"                    // 卫星图模式
        const val KEY_SHOW_TRAIL = "show_trail"                          // 显示轨迹尾迹
        const val KEY_TRAIL_COLOR = "trail_color"                        // 轨迹颜色
        const val KEY_EXPORT_FORMAT = "export_format"                    // 默认导出格式
        const val KEY_EXPORT_INCLUDE_PHOTOS = "export_include_photos"    // 导出包含照片
        const val KEY_BACKUP_ENABLED = "backup_enabled"                  // 云备份启用
        const val KEY_DARK_MODE = "dark_mode"                            // 深色模式
        const val KEY_LANGUAGE = "language"                              // 语言设置
        const val KEY_UNIT_SYSTEM = "unit_system"                        // 单位制（metric/imperial）
        
        // 默认值
        const val DEFAULT_RECORDING_INTERVAL = 1
        const val DEFAULT_MIN_DISTANCE_FILTER = 5
        const val DEFAULT_ACCURACY_THRESHOLD = 50
        const val DEFAULT_AUTO_PAUSE_SPEED = 1.0f  // m/s
    }
}

/**
 * 设置值类型
 */
enum class SettingType {
    STRING,
    INT,
    BOOLEAN,
    FLOAT,
    JSON
}

/**
 * 单位制系统
 */
enum class UnitSystem(val displayName: String) {
    METRIC("公制"),      // km, m, kg
    IMPERIAL("英制")     // miles, ft, lbs
}

/**
 * 地图提供商
 */
enum class MapProvider(val displayName: String) {
    GOOGLE("Google 地图"),
    AMAP("高德地图"),
    OSM("OpenStreetMap")
}
