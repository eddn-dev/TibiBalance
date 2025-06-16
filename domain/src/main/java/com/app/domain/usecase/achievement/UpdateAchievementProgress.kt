package com.app.domain.usecase.achievement

import com.app.domain.ids.AchievementId
import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject

/* C.  Actualizar progreso parcial (lo usa CheckUnlockAchievement) */
class UpdateAchievementProgress @Inject constructor(
    private val repo: AchievementsRepository
) { suspend operator fun invoke(id: AchievementId, progress: Int) =
    repo.updateProgress(id, progress) }
