/**
 * @file    RepositoryModule.kt
 * @ingroup data_di
 * @brief   Indica a Hilt qué implementación usar para cada repositorio.
 */
package com.app.data.di

import com.app.data.repository.EmotionRepositoryImpl
import com.app.domain.repository.HabitRepository
import com.app.data.repository.HabitRepositoryImpl
import com.app.data.repository.LocalDataRepositoryImpl
import com.app.data.repository.OnboardingRepositoryImpl
import com.app.domain.repository.EmotionRepository
import com.app.domain.repository.LocalDataRepository
import com.app.domain.repository.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** Vincula la interfaz `HabitRepository` con su implementación. */
    @Binds @Singleton
    abstract fun bindHabitRepository(
        impl: HabitRepositoryImpl
    ): HabitRepository

    @Binds @Singleton
    abstract fun bindsOnboardingRepo(
        impl: OnboardingRepositoryImpl
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindEmotionRepo(
        impl: EmotionRepositoryImpl
    ): EmotionRepository

    @Binds
    @Singleton
    abstract fun bindLocalRepo(impl: LocalDataRepositoryImpl): LocalDataRepository

}
