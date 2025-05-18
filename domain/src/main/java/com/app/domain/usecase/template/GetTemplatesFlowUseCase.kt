package com.app.domain.usecase.template

import com.app.domain.repository.HabitTemplateRepository
import javax.inject.Inject

// :domain/src/main/java/com/app/domain/usecase/template/GetTemplatesFlowUseCase.kt
class GetTemplatesFlowUseCase @Inject constructor(
    private val repo: HabitTemplateRepository
) { operator fun invoke() = repo.templates }

// + two one-liners
class RefreshTemplatesUseCase @Inject constructor(
    private val repo: HabitTemplateRepository
) { suspend operator fun invoke() = repo.refreshOnce() }

class StartTplSyncUseCase @Inject constructor(
    private val repo: HabitTemplateRepository
) { operator fun invoke() = repo.startSync() }
