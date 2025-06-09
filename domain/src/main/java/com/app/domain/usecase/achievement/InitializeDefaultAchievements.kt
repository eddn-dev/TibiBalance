package com.app.domain.usecase.achievement

import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject

/* A.  Sembrar logros tras el registro */
class InitializeDefaultAchievements @Inject constructor(
    private val achievementsRepo: AchievementsRepository
) {
    suspend operator fun invoke() {
        achievementsRepo.initializeDefaults()
    }
}
