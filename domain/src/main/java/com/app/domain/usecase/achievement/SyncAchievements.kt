package com.app.domain.usecase.achievement

import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject


/* D.  Sincronización manual / worker */
class SyncAchievements @Inject constructor(
    private val repo: AchievementsRepository
) { suspend operator fun invoke() = repo.syncNow() }
