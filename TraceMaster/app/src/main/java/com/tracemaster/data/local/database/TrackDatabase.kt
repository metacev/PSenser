package com.tracemaster.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tracemaster.data.local.dao.TrackDao
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import java.util.Date

/**
 * 类型转换器 - 用于 Room 数据库支持复杂类型
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromSportType(value: String): com.tracemaster.domain.model.SportType {
        return com.tracemaster.domain.model.SportType.valueOf(value)
    }

    @TypeConverter
    fun sportTypeToString(sportType: com.tracemaster.domain.model.SportType): String {
        return sportType.name
    }
}

/**
 * 轨迹数据库 - Room Database
 * 
 * 包含以下表：
 * 1. tracks - 轨迹主表
 * 2. track_points - 轨迹点表
 * 3. track_segments - 轨迹分段表
 * 4. track_photos - 轨迹照片表
 * 5. tags - 标签表
 * 6. track_tags - 轨迹标签关联表
 * 7. settings - 设置表
 */
@Database(
    entities = [
        Track::class,
        TrackPoint::class,
        TrackSegment::class,
        TrackPhoto::class,
        Tag::class,
        TrackTagCrossRef::class,
        Setting::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TrackDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun trackSegmentDao(): TrackSegmentDao
    abstract fun trackPhotoDao(): TrackPhotoDao
    abstract fun tagDao(): TagDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: TrackDatabase? = null

        fun getDatabase(context: Context): TrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackDatabase::class.java,
                    "track_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
