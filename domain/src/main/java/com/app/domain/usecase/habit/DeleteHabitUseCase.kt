/**
 * @file    DeleteHabitUseCase.kt
 * @ingroup domain_usecase_habit
 * @brief   Elimina un hábito (soft-delete por defecto).
 *
 * @details
 * - `hard = false` ➜ se escribe *tombstone* (`deletedAt`) y
 *   `pendingSync=true`; se mantendrá en Firestore para coherencia multi-device.
 * - `hard = true`  ➜ borra la fila sólo si nunca se subió (seguridad en repo).
 *
 * @param id   Identificador del hábito.
 * @param hard Forzar eliminación física (⚠ úsese con cuidado).
 */
package com.app.domain.usecase.habit

import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitRepository
import javax.inject.Inject

class DeleteHabitUseCase @Inject constructor(
    private val repo: HabitRepository
) {
    suspend operator fun invoke(
        id: HabitId,
        hard: Boolean = false
    ): Result<Unit> = repo.deleteHabit(id, hard)
}
