package com.app.data.di

import com.app.data.repository.WearConnectionRepositoryImpl
import com.app.domain.repository.WearConnectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/* :data/di/WearModule.kt */
@Module
@InstallIn(SingletonComponent::class)
abstract class WearModule {
    @Binds @Singleton
    abstract fun bindWearConnectionRepo(
        impl: WearConnectionRepositoryImpl
    ): WearConnectionRepository
}
