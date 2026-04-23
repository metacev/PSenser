package com.tracemaster.data.local.dao

import androidx.room.*
import com.tracemaster.domain.model.Tag
import com.tracemaster.domain.model.TrackTagCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * 标签数据访问对象
 */
@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY usageCount DESC, name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags ORDER BY usageCount DESC, name ASC")
    suspend fun getAllTagsSync(): List<Tag>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :keyword || '%' ORDER BY usageCount DESC")
    fun searchTags(keyword: String): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<Tag>)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE id = :tagId")
    suspend fun incrementUsageCount(tagId: Long)

    @Query("UPDATE tags SET usageCount = usageCount - 1 WHERE id = :tagId AND usageCount > 0")
    suspend fun decrementUsageCount(tagId: Long)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int

    // ==================== Track-Tag 关联操作 ====================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackTagCrossRef(crossRef: TrackTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackTagCrossRefs(crossRefs: List<TrackTagCrossRef>)

    @Delete
    suspend fun deleteTrackTagCrossRef(crossRef: TrackTagCrossRef)

    @Query("DELETE FROM track_tags WHERE trackId = :trackId AND tagId = :tagId")
    suspend fun deleteTrackTagCrossRef(trackId: Long, tagId: Long)

    @Query("DELETE FROM track_tags WHERE trackId = :trackId")
    suspend fun deleteAllTagsForTrack(trackId: Long)

    @Query("DELETE FROM track_tags WHERE tagId = :tagId")
    suspend fun deleteAllTracksForTag(tagId: Long)

    @Query("SELECT tagId FROM track_tags WHERE trackId = :trackId")
    fun getTagIdsByTrackId(trackId: Long): Flow<List<Long>>

    @Query("SELECT tagId FROM track_tags WHERE trackId = :trackId")
    suspend fun getTagIdsByTrackIdSync(trackId: Long): List<Long>

    @Query("SELECT trackId FROM track_tags WHERE tagId = :tagId")
    fun getTrackIdsByTagId(tagId: Long): Flow<List<Long>>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN track_tags tt ON t.id = tt.tagId
        WHERE tt.trackId = :trackId
        ORDER BY t.name ASC
    """)
    fun getTagsByTrackId(trackId: Long): Flow<List<Tag>>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN track_tags tt ON t.id = tt.tagId
        WHERE tt.trackId = :trackId
        ORDER BY t.name ASC
    """)
    suspend fun getTagsByTrackIdSync(trackId: Long): List<Tag>

    @Query("""
        SELECT DISTINCT t.* FROM tags t
        INNER JOIN track_tags tt ON t.id = tt.tagId
        WHERE tt.trackId IN (:trackIds)
        ORDER BY t.usageCount DESC
    """)
    suspend fun getTagsByTrackIds(trackIds: List<Long>): List<Tag>
}
