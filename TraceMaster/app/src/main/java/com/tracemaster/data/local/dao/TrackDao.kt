package com.tracemaster.data.local.dao

import androidx.room.*
import com.tracemaster.domain.model.SportType
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 轨迹数据访问对象
 */
@Dao
interface TrackDao {

    // ==================== Track 操作 ====================

    @Query("SELECT * FROM tracks WHERE isArchived = :isArchived ORDER BY startTime DESC")
    fun getAllTracks(isArchived: Boolean = false): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: Long): Track?

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackByIdFlow(trackId: Long): Flow<Track?>

    @Query("SELECT * FROM tracks WHERE sportType = :sportType ORDER BY startTime DESC")
    fun getTracksBySportType(sportType: SportType): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY startTime DESC")
    fun getFavoriteTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE name LIKE '%' || :keyword || '%' OR notes LIKE '%' || :keyword || '%' ORDER BY startTime DESC")
    fun searchTracks(keyword: String): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track): Long

    @Update
    suspend fun updateTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: Long)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean)

    @Query("UPDATE tracks SET isArchived = :isArchived WHERE id = :trackId")
    suspend fun toggleArchive(trackId: Long, isArchived: Boolean)

    @Query("UPDATE tracks SET totalDistance = :distance, totalTime = :time, pointCount = :count, updateTime = :updateTime WHERE id = :trackId")
    suspend fun updateTrackStats(trackId: Long, distance: Double, time: Long, count: Int, updateTime: Date)

    @Query("UPDATE tracks SET endTime = :endTime, updateTime = :updateTime WHERE id = :trackId")
    suspend fun finishTrack(trackId: Long, endTime: Date, updateTime: Date)

    @Query("SELECT COUNT(*) FROM tracks")
    fun getTotalTrackCount(): Flow<Int>

    @Query("SELECT SUM(totalDistance) FROM tracks WHERE startTime >= :startTime AND startTime <= :endTime")
    suspend fun getTotalDistanceInRange(startTime: Date, endTime: Date): Double?

    // ==================== TrackPoint 操作 ====================

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY pointIndex ASC")
    fun getPointsByTrackId(trackId: Long): Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY pointIndex ASC")
    suspend fun getPointsByTrackIdSync(trackId: Long): List<TrackPoint>

    @Query("SELECT * FROM track_points WHERE trackId = :trackId AND segmentIndex = :segmentIndex ORDER BY pointIndex ASC")
    suspend fun getPointsBySegment(trackId: Long, segmentIndex: Int): List<TrackPoint>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackPoint(point: TrackPoint): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackPoints(points: List<TrackPoint>)

    @Update
    suspend fun updateTrackPoint(point: TrackPoint)

    @Delete
    suspend fun deleteTrackPoint(point: TrackPoint)

    @Query("DELETE FROM track_points WHERE trackId = :trackId")
    suspend fun deleteAllPointsForTrack(trackId: Long)

    @Query("SELECT MAX(pointIndex) FROM track_points WHERE trackId = :trackId")
    suspend fun getMaxPointIndex(trackId: Long): Int?

    @Query("SELECT MAX(speed) FROM track_points WHERE trackId = :trackId")
    suspend fun getMaxSpeed(trackId: Long): Float?

    @Query("SELECT MIN(altitude) FROM track_points WHERE trackId = :trackId")
    suspend fun getMinAltitude(trackId: Long): Double?

    @Query("SELECT MAX(altitude) FROM track_points WHERE trackId = :trackId")
    suspend fun getMaxAltitude(trackId: Long): Double?

    @Query("SELECT COUNT(*) FROM track_points WHERE trackId = :trackId")
    suspend fun getPointCount(trackId: Long): Int

    // ==================== 批量操作 ====================

    @Transaction
    suspend fun insertTrackWithPoints(track: Track, points: List<TrackPoint>): Long {
        val trackId = insertTrack(track)
        val pointsWithTrackId = points.map { it.copy(trackId = trackId) }
        insertTrackPoints(pointsWithTrackId)
        return trackId
    }

    @Transaction
    suspend fun deleteTrackWithPoints(track: Track) {
        deleteAllPointsForTrack(track.id)
        deleteTrack(track)
    }
}
