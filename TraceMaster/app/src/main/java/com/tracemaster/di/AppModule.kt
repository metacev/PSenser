package com.tracemaster.di

import android.content.Context
import com.tracemaster.data.local.dao.TrackDao
import com.tracemaster.data.local.database.TrackDatabase
import com.tracemaster.data.repository.TrackRepository
import com.tracemaster.data.repository.TrackRepositoryImpl
import com.tracemaster.domain.manager.SubscriptionManager
import com.tracemaster.util.location.LocationManager
import com.tracemaster.util.map.AMapManager
import com.tracemaster.util.permissions.PermissionManager
import com.tracemaster.util.security.AntiCheatEngine
import com.tracemaster.util.security.PrivacyDesensitizationEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块 - 提供单例对象
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTrackDatabase(@ApplicationContext context: Context): TrackDatabase {
        // 默认启用加密，可通过 BuildConfig 控制
        return TrackDatabase.getDatabase(context, useEncryption = true)
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: TrackDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun provideTrackSegmentDao(database: TrackDatabase) = database.trackSegmentDao()

    @Provides
    @Singleton
    fun provideTrackPhotoDao(database: TrackDatabase) = database.trackPhotoDao()

    @Provides
    @Singleton
    fun provideTagDao(database: TrackDatabase) = database.tagDao()

    @Provides
    @Singleton
    fun provideSettingDao(database: TrackDatabase) = database.settingDao()

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return LocationManager(context)
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideTrackRepository(
        trackDao: TrackDao,
        locationManager: LocationManager
    ): TrackRepository {
        return TrackRepositoryImpl(trackDao, locationManager)
    }
    
    @Provides
    @Singleton
    fun provideAMapManager(): AMapManager {
        return AMapManager()
    }

    @Provides
    @Singleton
    fun provideSubscriptionManager(): SubscriptionManager {
        return SubscriptionManager()
    }

    @Provides
    @Singleton
    fun provideAntiCheatEngine(): AntiCheatEngine {
        return AntiCheatEngine()
    }

    @Provides
    @Singleton
    fun providePrivacyDesensitizationEngine(): PrivacyDesensitizationEngine {
        return PrivacyDesensitizationEngine()
    }
}
