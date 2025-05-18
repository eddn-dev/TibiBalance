/**
 * @file    SyncHabitsUseCase.kt
 * @ingroup domain_usecase_habit
 * @brief   Fuerza reconciliación inmediata Room ↔ Firestore.
 *
 * @details
 * Normalmente lo invoca un botón “Sincronizar ahora” o tests; el
 * `HabitSyncWorker` periódicamente llama a la misma función.
 */
package com.app.domain.usecase.habit

import com.app.domain.repository.HabitRepository
import javax.inject.Inject

class SyncHabitsUseCase @Inject constructor(
    private val repo: HabitRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.syncNow()
}
