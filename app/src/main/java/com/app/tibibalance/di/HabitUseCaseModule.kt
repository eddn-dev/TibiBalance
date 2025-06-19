/**
 * @file    HabitUseCaseModule.kt
 * @ingroup domain_di
 * @brief   Provee los casos de uso de hábitos con Hilt.
 */
package com.app.tibibalance.di

import com.app.domain.usecase.habit.*
import com.app.domain.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
/*
@Module
@InstallIn(SingletonComponent::class)
object HabitUseCaseModule {

    @Provides fun provideGetHabitsFlow(repo: HabitRepository)     = GetHabitsFlow(repo)
    @Provides fun provideCreateHabit(repo: HabitRepository)       = CreateHabit(repo)
    @Provides fun provideUpdateHabit(repo: HabitRepository)       = UpdateHabit(
        repo,
        getHabit = GetHabitById(repo)
    )
    @Provides fun provideDeleteHabit(repo: HabitRepository)       = DeleteHabit(repo)
}
*/