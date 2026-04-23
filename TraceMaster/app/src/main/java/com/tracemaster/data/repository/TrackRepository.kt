package com.tracemaster.data.repository

import com.tracemaster.domain.model.*
import com.tracemaster.util.location.LocationData
import kotlinx.coroutines.flow.Flow

/**
 * 轨迹仓库接口 - 定义数据操作契约
 */
interface TrackRepository {

    // ==================== Track 操作 ====================

    /**
     * 获取所有轨迹列表
     */
    fun getAllTracks(isArchived: Boolean = false): Flow<List<Track>>

    /**
     * 根据 ID 获取轨迹
     */
    suspend fun getTrackById(trackId: Long): Track?

    /**
     * 获取轨迹详情（带点数据）
     */
    suspend fun getTrackWithPoints(trackId: Long): Pair<Track, List<TrackPoint>>?

    /**
     * 获取收藏的轨迹
     */
    fun getFavoriteTracks(): Flow<List<Track>>

    /**
     * 搜索轨迹
     */
    fun searchTracks(keyword: String): Flow<List<Track>>

    /**
     * 插入新轨迹
     */
    suspend fun insertTrack(track: Track): Long

    /**
     * 更新轨迹
     */
    suspend fun updateTrack(track: Track)

    /**
     * 删除轨迹
     */
    suspend fun deleteTrack(track: Track)

    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(trackId: Long)

    /**
     * 切换归档状态
     */
    suspend fun toggleArchive(trackId: Long)

    // ==================== TrackPoint 操作 ====================

    /**
     * 获取轨迹的所有点
     */
    fun getPointsByTrackId(trackId: Long): Flow<List<TrackPoint>>

    /**
     * 插入轨迹点
     */
    suspend fun insertTrackPoint(point: TrackPoint)

    /**
     * 批量插入轨迹点
     */
    suspend fun insertTrackPoints(points: List<TrackPoint>)

    // ==================== 录制相关 ====================

    /**
     * 开始新的轨迹录制
     */
    suspend fun startRecording(sportType: SportType): Track

    /**
     * 暂停录制
     */
    suspend fun pauseRecording(trackId: Long)

    /**
     * 继续录制
     */
    suspend fun resumeRecording(trackId: Long)

    /**
     * 停止录制并完成轨迹
     */
    suspend fun stopRecording(trackId: Long)

    /**
     * 添加定位点到当前轨迹
     */
    suspend fun addLocationPoint(
        trackId: Long,
        locationData: LocationData,
        pointIndex: Int
    ): TrackPoint

    /**
     * 计算两点间距离 (Haversine 公式)
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double

    /**
     * 获取未完成的录制轨迹（用于崩溃恢复）
     */
    suspend fun getUnfinishedTrack(): Track?

    // ==================== Tag 操作 ====================

    /**
     * 获取所有标签
     */
    fun getAllTags(): Flow<List<Tag>>

    /**
     * 插入标签
     */
    suspend fun insertTag(tag: Tag): Long

    /**
     * 更新标签
     */
    suspend fun updateTag(tag: Tag)

    /**
     * 删除标签
     */
    suspend fun deleteTag(tag: Tag)

    /**
     * 为轨迹添加标签
     */
    suspend fun addTagToTrack(trackId: Long, tagId: Long)

    /**
     * 从轨迹移除标签
     */
    suspend fun removeTagFromTrack(trackId: Long, tagId: Long)

    /**
     * 获取轨迹的标签
     */
    fun getTagsByTrackId(trackId: Long): Flow<List<Tag>>

    // ==================== Setting 操作 ====================

    /**
     * 获取设置值
     */
    suspend fun getSettingValue(key: String): String?

    /**
     * 保存设置值
     */
    suspend fun saveSetting(key: String, value: String, type: SettingType = SettingType.STRING)

    /**
     * 获取布尔设置值
     */
    suspend fun getBooleanSetting(key: String, defaultValue: Boolean = false): Boolean

    /**
     * 获取整数设置值
     */
    suspend fun getIntSetting(key: String, defaultValue: Int = 0): Int

    // ==================== TrackPhoto 操作 ====================

    /**
     * 添加轨迹照片
     */
    suspend fun addTrackPhoto(photo: TrackPhoto): Long

    /**
     * 获取轨迹的照片
     */
    fun getPhotosByTrackId(trackId: Long): Flow<List<TrackPhoto>>

    /**
     * 删除轨迹照片
     */
    suspend fun deleteTrackPhoto(photo: TrackPhoto)

    /**
     * 设置封面照片
     */
    suspend fun setCoverPhoto(photoId: Long)
}
