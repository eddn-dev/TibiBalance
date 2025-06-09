package com.app.data.di

import com.app.data.local.db.AppDb
import com.app.data.metrics.local.UserMetricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo Hilt que expone únicamente UserMetricsDao.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideUserMetricsDao(db: AppDb): UserMetricsDao =
        db.userMetricsDao()
}
