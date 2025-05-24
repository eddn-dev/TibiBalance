/**
 * @file    RemoteModule.kt
 * @ingroup data_di
 * @brief   Provee la fuente remota mediante Hilt.
 */
package com.app.data.di

import com.app.data.remote.datasource.FirebaseHabitRemoteDataSource
import com.app.data.remote.datasource.HabitRemoteDataSource
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
}
