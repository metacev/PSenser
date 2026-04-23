package com.tracemaster.data.repository

import com.tracemaster.domain.model.SportType
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
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
}
