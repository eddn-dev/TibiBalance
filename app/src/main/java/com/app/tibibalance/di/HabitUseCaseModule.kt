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

@Module @InstallIn(ViewModelComponent::class)
object HabitUseCaseModule {

    @Provides fun provideGetHabitsFlow(repo: HabitRepository)     = GetHabitsFlowUseCase(repo)
    @Provides fun provideCreateHabit(repo: HabitRepository)       = CreateHabitUseCase(repo)
    @Provides fun provideUpdateHabit(repo: HabitRepository)       = UpdateHabitUseCase(repo)
    @Provides fun provideDeleteHabit(repo: HabitRepository)       = DeleteHabitUseCase(repo)
    @Provides fun provideMarkCompleted(repo: HabitRepository)     = MarkCompletedUseCase(repo)
    @Provides fun provideSyncHabits(repo: HabitRepository)        = SyncHabitsUseCase(repo)
}
