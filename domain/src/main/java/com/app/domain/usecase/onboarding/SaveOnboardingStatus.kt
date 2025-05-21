/**
 * @file    SaveOnboardingStatus.kt
 * @ingroup domain_usecase_onboarding
 * @brief   Use Case que persiste cambios en onboarding.
 */
package com.app.domain.usecase.onboarding

import com.app.domain.entities.OnboardingStatus
import com.app.domain.repository.OnboardingRepository
import javax.inject.Inject

/** Graba el nuevo estado de onboarding (y dispara sync). */
class SaveOnboardingStatus @Inject constructor(
    private val repo: OnboardingRepository
) {
    suspend operator fun invoke(uid: String, status: OnboardingStatus) {
        repo.save(uid, status)
    }
}
