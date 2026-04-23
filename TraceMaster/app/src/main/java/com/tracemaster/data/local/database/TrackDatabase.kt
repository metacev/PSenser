package com.tracemaster.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tracemaster.data.local.dao.TrackDao
import com.tracemaster.domain.model.Track
import com.tracemaster.domain.model.TrackPoint
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
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
 * 轨迹数据库 - Room Database with SQLCipher Encryption
 *
 * 包含以下表：
 * 1. tracks - 轨迹主表
 * 2. track_points - 轨迹点表
 * 3. track_segments - 轨迹分段表
 * 4. track_photos - 轨迹照片表
 * 5. tags - 标签表
 * 6. track_tags - 轨迹标签关联表
 * 7. settings - 设置表
 *
 * 使用 SQLCipher 进行数据库加密，保护用户隐私数据
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
        
        // 数据库加密密码（实际应用中应该从安全存储中获取）
        private const val DB_PASSWORD = "TraceMaster_Secure_Key_2024!"

        fun getDatabase(context: Context, useEncryption: Boolean = true): TrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = if (useEncryption) {
                    // 使用 SQLCipher 加密数据库
                    val factory = SupportFactory(SQLiteDatabase.getBytes(DB_PASSWORD.toCharArray()))
                    Room.databaseBuilder(
                        context.applicationContext,
                        TrackDatabase::class.java,
                        "track_database_encrypted.db"
                    )
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                db.setVersion(1)
                            }
                        })
                        .build()
                } else {
                    // 不加密的普通数据库（仅用于调试）
                    Room.databaseBuilder(
                        context.applicationContext,
                        TrackDatabase::class.java,
                        "track_database.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                INSTANCE = instance
                instance
            }
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
