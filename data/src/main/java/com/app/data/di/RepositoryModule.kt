/**
 * @file    RepositoryModule.kt
 * @ingroup data_di
 * @brief   Indica a Hilt qué implementación usar para cada repositorio.
 */
package com.app.data.di

import com.app.domain.repository.HabitRepository
import com.app.data.repository.HabitRepositoryImpl
import com.app.data.repository.HabitTemplateRepositoryImpl
import com.app.domain.repository.HabitTemplateRepository
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
    abstract fun bindHabitTemplateRepository(
        impl: HabitTemplateRepositoryImpl
    ): HabitTemplateRepository
}
