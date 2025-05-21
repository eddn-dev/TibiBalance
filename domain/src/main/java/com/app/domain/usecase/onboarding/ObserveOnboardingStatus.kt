/**
 * @file    ObserveOnboardingStatus.kt
 * @ingroup domain_usecase_onboarding
 * @brief   Use Case para observar el estado de onboarding.
 */
package com.app.domain.usecase.onboarding

import com.app.domain.entities.OnboardingStatus
import com.app.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Devuelve un flujo con el progreso de onboarding del usuario. */
class ObserveOnboardingStatus @Inject constructor(
    private val repo: OnboardingRepository
) {
    operator fun invoke(uid: String): Flow<OnboardingStatus> =
        repo.observe(uid)
}
