package com.tracemaster.di

import android.content.Context
import com.tracemaster.data.local.dao.TrackDao
import com.tracemaster.data.local.database.TrackDatabase
import com.tracemaster.data.repository.TrackRepository
import com.tracemaster.data.repository.TrackRepositoryImpl
import com.tracemaster.util.location.LocationManager
import dagger.Binds
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
        return TrackDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: TrackDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return LocationManager(context)
    }

    @Provides
    @Singleton
    fun provideTrackRepository(
        trackDao: TrackDao,
        locationManager: LocationManager
    ): TrackRepository {
        return TrackRepositoryImpl(trackDao, locationManager)
    }
}
