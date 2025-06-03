package com.app.domain.usecase.user

import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject

class InitializeAchievementsUseCase @Inject constructor (
    private val achievementsRepository: AchievementsRepository
) {
    suspend operator fun invoke(userId: String) {
        achievementsRepository.initializeAchievementsIfMissing(userId)
    }
}
