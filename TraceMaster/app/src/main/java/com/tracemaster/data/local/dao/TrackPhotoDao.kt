package com.tracemaster.data.local.dao

import androidx.room.*
import com.tracemaster.domain.model.TrackPhoto
import kotlinx.coroutines.flow.Flow

/**
 * 轨迹照片数据访问对象
 */
@Dao
interface TrackPhotoDao {

    @Query("SELECT * FROM track_photos WHERE trackId = :trackId ORDER BY timestamp ASC")
    fun getPhotosByTrackId(trackId: Long): Flow<List<TrackPhoto>>

    @Query("SELECT * FROM track_photos WHERE trackId = :trackId ORDER BY timestamp ASC")
    suspend fun getPhotosByTrackIdSync(trackId: Long): List<TrackPhoto>

    @Query("SELECT * FROM track_photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: Long): TrackPhoto?

    @Query("SELECT * FROM track_photos WHERE trackId = :trackId AND isCover = 1 LIMIT 1")
    suspend fun getCoverPhoto(trackId: Long): TrackPhoto?

    @Query("SELECT * FROM track_photos WHERE pointId = :pointId")
    fun getPhotosByPointId(pointId: Long): Flow<List<TrackPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: TrackPhoto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<TrackPhoto>)

    @Update
    suspend fun updatePhoto(photo: TrackPhoto)

    @Delete
    suspend fun deletePhoto(photo: TrackPhoto)

    @Query("DELETE FROM track_photos WHERE trackId = :trackId")
    suspend fun deleteAllPhotosForTrack(trackId: Long)

    @Query("DELETE FROM track_photos WHERE pointId = :pointId")
    suspend fun deletePhotosByPointId(pointId: Long)

    @Query("UPDATE track_photos SET isCover = 0 WHERE trackId = :trackId")
    suspend fun clearCoverPhoto(trackId: Long)

    @Query("UPDATE track_photos SET isCover = 1 WHERE id = :photoId")
    suspend fun setAsCoverPhoto(photoId: Long)

    @Query("SELECT COUNT(*) FROM track_photos WHERE trackId = :trackId")
    suspend fun getPhotoCount(trackId: Long): Int

    @Query("SELECT SUM(fileSize) FROM track_photos WHERE trackId = :trackId")
    suspend fun getTotalPhotoSize(trackId: Long): Long?
}
