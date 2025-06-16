/* domain/usecase/onboarding/SyncOnboarding.kt */
package com.app.domain.usecase.onboarding

import com.app.domain.repository.OnboardingRepository
import javax.inject.Inject

class SyncOnboarding @Inject constructor(
    private val repo: OnboardingRepository
) {
    /** Onboarding vive en users/{uid}/onboarding → se pasa uid explícito. */
    suspend operator fun invoke(uid: String): Result<Unit> = repo.syncNow(uid)
}
