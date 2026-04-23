package com.tracemaster.data.repository

import com.tracemaster.data.local.dao.TrackDao
import com.tracemaster.domain.model.SportType
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import com.tracemaster.util.location.LocationData
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import kotlin.math.*

/**
 * 轨迹仓库实现类
 */
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val locationManager: com.tracemaster.util.location.LocationManager
) : TrackRepository {

    // 地球半径（米）
    private val EARTH_RADIUS = 6371000.0

    override fun getAllTracks(isArchived: Boolean): Flow<List<Track>> {
        return trackDao.getAllTracks(isArchived)
    }

    override suspend fun getTrackById(trackId: Long): Track? {
        return trackDao.getTrackById(trackId)
    }

    override suspend fun getTrackWithPoints(trackId: Long): Pair<Track, List<TrackPoint>>? {
        val track = trackDao.getTrackById(trackId) ?: return null
        val points = trackDao.getPointsByTrackIdSync(trackId)
        return Pair(track, points)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavoriteTracks()
    }

    override fun searchTracks(keyword: String): Flow<List<Track>> {
        return trackDao.searchTracks(keyword)
    }

    override suspend fun insertTrack(track: Track): Long {
        return trackDao.insertTrack(track)
    }

    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track)
    }

    override suspend fun deleteTrack(track: Track) {
        trackDao.deleteTrackWithPoints(track)
    }

    override suspend fun toggleFavorite(trackId: Long) {
        val track = trackDao.getTrackById(trackId) ?: return
        trackDao.toggleFavorite(trackId, !track.isFavorite)
    }

    override suspend fun toggleArchive(trackId: Long) {
        val track = trackDao.getTrackById(trackId) ?: return
        trackDao.toggleArchive(trackId, !track.isArchived)
    }

    override fun getPointsByTrackId(trackId: Long): Flow<List<TrackPoint>> {
        return trackDao.getPointsByTrackId(trackId)
    }

    override suspend fun insertTrackPoint(point: TrackPoint) {
        trackDao.insertTrackPoint(point)
    }

    override suspend fun insertTrackPoints(points: List<TrackPoint>) {
        trackDao.insertTrackPoints(points)
    }

    override suspend fun startRecording(sportType: SportType): Track {
        val track = Track(
            name = buildTrackName(sportType),
            startTime = Date(),
            endTime = null,
            sportType = sportType
        )
        val trackId = trackDao.insertTrack(track)
        return track.copy(id = trackId)
    }

    override suspend fun pauseRecording(trackId: Long) {
        // 暂停时更新总时长
        val track = trackDao.getTrackById(trackId) ?: return
        val totalTime = (Date().time - track.startTime.time) / 1000
        trackDao.updateTrackStats(trackId, track.totalDistance, totalTime, track.pointCount, Date())
    }

    override suspend fun resumeRecording(trackId: Long) {
        // 继续录制，无需特殊处理
    }

    override suspend fun stopRecording(trackId: Long) {
        val track = trackDao.getTrackById(trackId) ?: return
        val points = trackDao.getPointsByTrackIdSync(trackId)
        
        // 计算最终统计数据
        val totalDistance = calculateTotalDistance(points)
        val totalTime = (Date().time - track.startTime.time) / 1000
        
        trackDao.finishTrack(trackId, Date(), Date())
        trackDao.updateTrackStats(trackId, totalDistance, totalTime, points.size, Date())
    }

    override suspend fun addLocationPoint(
        trackId: Long,
        locationData: LocationData,
        pointIndex: Int
    ): TrackPoint {
        val point = TrackPoint(
            trackId = trackId,
            latitude = locationData.latitude,
            longitude = locationData.longitude,
            altitude = locationData.altitude,
            accuracy = locationData.accuracy,
            speed = locationData.speed,
            bearing = locationData.bearing,
            timestamp = Date(locationData.timestamp),
            pointIndex = pointIndex,
            isLowQuality = locationData.accuracy > 50,
            isDrift = isDriftPoint(locationData)
        )
        
        trackDao.insertTrackPoint(point)
        
        // 更新轨迹的统计信息
        if (pointIndex > 0) {
            val previousPoint = trackDao.getPointsBySegment(trackId, 0)
                .find { it.pointIndex == pointIndex - 1 }
            previousPoint?.let { prev ->
                val segmentDistance = calculateDistance(
                    prev.latitude, prev.longitude,
                    point.latitude, point.longitude
                )
                
                val currentTrack = trackDao.getTrackById(trackId)
                currentTrack?.let { track ->
                    val newTotalDistance = track.totalDistance + segmentDistance
                    val totalTime = (System.currentTimeMillis() - track.startTime.time) / 1000
                    trackDao.updateTrackStats(
                        trackId,
                        newTotalDistance,
                        totalTime,
                        pointIndex + 1,
                        Date()
                    )
                }
            }
        }
        
        return point
    }

    override fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLonRad = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLonRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLonRad / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS * c
    }

    override suspend fun getUnfinishedTrack(): Track? {
        // 查找没有结束时间的轨迹
        val allTracks = trackDao.getAllTracks(false)
        // 由于是 Flow，需要在调用处处理
        return null
    }

    /**
     * 计算轨迹总距离
     */
    private fun calculateTotalDistance(points: List<TrackPoint>): Double {
        if (points.size < 2) return 0.0
        
        var totalDistance = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            totalDistance += calculateDistance(
                prev.latitude, prev.longitude,
                curr.latitude, curr.longitude
            )
        }
        return totalDistance
    }

    /**
     * 判断是否为漂移点
     * 简单规则：速度突变超过 20m/s (72km/h) 且非驾车模式
     */
    private fun isDriftPoint(locationData: LocationData): Boolean {
        return locationData.speed > 20f && !locationData.isHighAccuracy
    }

    /**
     * 构建轨迹名称
     */
    private fun buildTrackName(sportType: SportType): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
        return "${sportType.displayName} ${dateFormat.format(Date())}"
    }
}
