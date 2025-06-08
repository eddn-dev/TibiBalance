package com.app.domain.usecase.tutorial

import com.app.domain.repository.OnboardingRepository
import javax.inject.Inject

/** Saves the completion state of the interactive tutorial. */
class SaveTutorialStatusUseCase @Inject constructor(
    private val repo: OnboardingRepository
) {
    suspend operator fun invoke(uid: String, completed: Boolean) {
        repo.saveTutorialStatus(uid, completed)
    }
}
