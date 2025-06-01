package com.app.domain.usecase.user

import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject

class UnlockAchievementUseCase @Inject constructor(
    private val achievementsRepository: AchievementsRepository
) {
    suspend operator fun invoke(userId: String, achievementId: String): Boolean {
        return achievementsRepository.unlockIfNotYet(userId, achievementId)
    }

    suspend fun updateProgress(userId: String, achievementId: String, progress: Int) {
        achievementsRepository.updateProgress(userId, achievementId, progress)
    }
}