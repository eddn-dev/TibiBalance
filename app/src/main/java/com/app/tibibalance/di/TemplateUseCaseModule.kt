package com.app.tibibalance.di

import com.app.domain.repository.HabitTemplateRepository
import com.app.domain.usecase.template.GetTemplatesFlowUseCase
import com.app.domain.usecase.template.RefreshTemplatesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module @InstallIn(ViewModelComponent::class)
object TemplateUseCaseModule {
    @Provides fun provideTplFlow(repo: HabitTemplateRepository)  = GetTemplatesFlowUseCase(repo)
    @Provides fun provideTplRefresh(repo: HabitTemplateRepository) = RefreshTemplatesUseCase(repo)
}
