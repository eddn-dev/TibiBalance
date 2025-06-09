package com.app.domain.usecase.achievement

import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject

/* B.  Observación reactiva */
class ObserveAchievements @Inject constructor(
    private val repo: AchievementsRepository
) { operator fun invoke() = repo.observeAll() }