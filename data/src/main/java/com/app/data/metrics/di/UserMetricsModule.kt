package com.app.data.metrics.di

import com.app.data.metrics.repository.UserMetricsRepositoryImpl
import com.app.domain.metrics.repository.UserMetricsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para inyectar UserMetricsRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UserMetricsModule {

    @Binds
    @Singleton
    abstract fun bindUserMetricsRepository(
        impl: UserMetricsRepositoryImpl
    ): UserMetricsRepository
}
