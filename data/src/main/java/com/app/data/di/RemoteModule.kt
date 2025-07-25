/**
 * @file    RemoteModule.kt
 * @ingroup data_di
 * @brief   Provee la fuente remota mediante Hilt.
 */
package com.app.data.di

import com.app.data.remote.datasource.AchievementRemoteDataSource
import com.app.data.remote.datasource.DailyTipsRemoteDataSource
import com.app.data.remote.datasource.FirebaseAchievementRemoteDataSource
import com.app.data.remote.datasource.FirebaseDailyTipsRemoteDataSource
import com.app.data.remote.datasource.FirebaseHabitActivityRemoteDataSource
import com.app.data.remote.datasource.FirebaseHabitRemoteDataSource
import com.app.data.remote.datasource.FirebaseMetricsRemoteDataSource
import com.app.data.remote.datasource.HabitActivityRemoteDataSource
import com.app.data.remote.datasource.HabitRemoteDataSource
import com.app.data.remote.datasource.MetricsRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {
    @Binds @Singleton
    abstract fun bindHabitRemoteDataSource(
        impl: FirebaseHabitRemoteDataSource
    ): HabitRemoteDataSource

    @Binds @Singleton
    abstract fun bindDailyTipsRemoteDataSource(
        impl: FirebaseDailyTipsRemoteDataSource
    ): DailyTipsRemoteDataSource

    @Binds @Singleton
    abstract fun bindHabitActivityRemoteDataSource(
        impl: FirebaseHabitActivityRemoteDataSource
    ): HabitActivityRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAchievementRemoteDataSource(
        impl: FirebaseAchievementRemoteDataSource
    ): AchievementRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMetricsRemoteDataSource(
        impl: FirebaseMetricsRemoteDataSource
    ): MetricsRemoteDataSource

}
