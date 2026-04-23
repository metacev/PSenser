package com.tracemaster.data.local.dao

import androidx.room.*
import com.tracemaster.domain.model.Setting
import kotlinx.coroutines.flow.Flow

/**
 * 设置数据访问对象
 */
@Dao
interface SettingDao {

    @Query("SELECT * FROM settings ORDER BY key ASC")
    fun getAllSettings(): Flow<List<Setting>>

    @Query("SELECT * FROM settings ORDER BY key ASC")
    suspend fun getAllSettingsSync(): List<Setting>

    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    suspend fun getSettingByKey(key: String): Setting?

    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    fun getSettingByKeyFlow(key: String): Flow<Setting?>

    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    fun getSettingValueFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<Setting>)

    @Update
    suspend fun updateSetting(setting: Setting)

    @Delete
    suspend fun deleteSetting(setting: Setting)

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteSettingByKey(key: String)

    @Query("""
        INSERT OR REPLACE INTO settings (key, value, type, description, updateTime)
        VALUES (:key, :value, :type, :description, :updateTime)
    """)
    suspend fun upsertSetting(
        key: String,
        value: String,
        type: com.tracemaster.domain.model.SettingType = com.tracemaster.domain.model.SettingType.STRING,
        description: String? = null,
        updateTime: java.util.Date = java.util.Date()
    )

    @Query("SELECT COUNT(*) FROM settings")
    suspend fun getSettingCount(): Int

    // ==================== 便捷方法 ====================

    @Query("SELECT value FROM settings WHERE key = 'recording_interval' LIMIT 1")
    suspend fun getRecordingInterval(): Int?

    @Query("SELECT value FROM settings WHERE key = 'min_distance_filter' LIMIT 1")
    suspend fun getMinDistanceFilter(): Int?

    @Query("SELECT value FROM settings WHERE key = 'accuracy_threshold' LIMIT 1")
    suspend fun getAccuracyThreshold(): Int?

    @Query("SELECT value FROM settings WHERE key = 'auto_pause_enabled' LIMIT 1")
    suspend fun isAutoPauseEnabled(): Boolean?

    @Query("SELECT value FROM settings WHERE key = 'keep_screen_on' LIMIT 1")
    suspend fun isKeepScreenOn(): Boolean?

    @Query("SELECT value FROM settings WHERE key = 'voice_feedback' LIMIT 1")
    suspend fun isVoiceFeedbackEnabled(): Boolean?

    @Query("SELECT value FROM settings WHERE key = 'map_provider' LIMIT 1")
    suspend fun getMapProvider(): String?

    @Query("SELECT value FROM settings WHERE key = 'export_format' LIMIT 1")
    suspend fun getExportFormat(): String?
}
