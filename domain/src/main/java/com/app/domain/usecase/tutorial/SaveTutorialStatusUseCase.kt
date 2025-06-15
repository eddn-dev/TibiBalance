package com.app.domain.usecase.tutorial

import com.app.domain.entities.OnboardingStatus
import com.app.domain.repository.OnboardingRepository
import javax.inject.Inject

/** Saves the completion state of the interactive tutorial. */
class SaveTutorialStatusUseCase @Inject constructor(
    private val repo: OnboardingRepository
) {
    suspend operator fun invoke(uid: String, update: (OnboardingStatus) -> OnboardingStatus) {
        // 1️⃣ Asegúrate de tener los datos más recientes del servidor
        repo.syncNow(uid)

        // 2️⃣ Lee el estado actualizado (post-sync)
        val current = repo.getStatus(uid)

        // 3️⃣ Aplica el cambio incremental
        val updated = update(current)

        // 4️⃣ Guarda la versión fusionada
        repo.saveStatus(uid, updated)
    }
}