package com.app.domain.usecase.auth

import com.app.domain.repository.AuthRepository
import com.app.domain.repository.EmotionRepository
import com.app.domain.repository.HabitRepository
import com.app.domain.repository.OnboardingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncAccount @Inject constructor(
    private val habitRepo : HabitRepository,
    private val emotionRepo: EmotionRepository,
    private val onboardingRepo: OnboardingRepository,
    private val authRepo : AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = authRepo.authState().first()
            ?: return@withContext Result.failure(IllegalStateException("No user"))

        runCatching {
            habitRepo.syncNow().getOrThrow()
            emotionRepo.syncNow().getOrThrow()
            onboardingRepo.syncNow(uid).getOrThrow()
        }.onFailure {
            it.printStackTrace()
        }
    }
}
