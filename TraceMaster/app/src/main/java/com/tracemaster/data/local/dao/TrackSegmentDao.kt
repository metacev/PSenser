package com.tracemaster.data.local.dao

import androidx.room.*
import com.tracemaster.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 轨迹分段数据访问对象
 */
@Dao
interface TrackSegmentDao {

    @Query("SELECT * FROM track_segments WHERE trackId = :trackId ORDER BY segmentIndex ASC")
    fun getSegmentsByTrackId(trackId: Long): Flow<List<TrackSegment>>

    @Query("SELECT * FROM track_segments WHERE trackId = :trackId ORDER BY segmentIndex ASC")
    suspend fun getSegmentsByTrackIdSync(trackId: Long): List<TrackSegment>

    @Query("SELECT * FROM track_segments WHERE id = :segmentId")
    suspend fun getSegmentById(segmentId: Long): TrackSegment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: TrackSegment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TrackSegment>)

    @Update
    suspend fun updateSegment(segment: TrackSegment)

    @Delete
    suspend fun deleteSegment(segment: TrackSegment)

    @Query("DELETE FROM track_segments WHERE trackId = :trackId")
    suspend fun deleteAllSegmentsForTrack(trackId: Long)

    @Query("SELECT COUNT(*) FROM track_segments WHERE trackId = :trackId")
    suspend fun getSegmentCount(trackId: Long): Int

    @Query("SELECT SUM(distance) FROM track_segments WHERE trackId = :trackId")
    suspend fun getTotalDistance(trackId: Long): Double?

    @Query("SELECT SUM(duration) FROM track_segments WHERE trackId = :trackId")
    suspend fun getTotalDuration(trackId: Long): Long?
}
