/**
 * @file    UpdateHabitUseCase.kt
 * @ingroup domain_usecase_habit
 * @brief   Actualiza un hábito y marca la fila con pendingSync=true.
 *
 * @details
 * - El repositorio actualiza `meta.updatedAt` automáticamente.
 * - Se sigue la política LWW por timestamp cuando se sube a Firestore.
 */
package com.app.domain.usecase.habit

import com.app.domain.entities.Habit
import com.app.domain.repository.HabitRepository
import javax.inject.Inject

class UpdateHabit @Inject constructor(
    private val repo: HabitRepository
) { suspend operator fun invoke(h: Habit) {
    require(h.challenge == null) { "Hábitos en reto no se pueden editar" }
    repo.update(h)
} }
