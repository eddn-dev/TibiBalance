/**
 * @file    MarkCompletedUseCase.kt
 * @ingroup domain_usecase_habit
 * @brief   Registra la finalización (o skip/reset) de una sesión de hábito.
 *
 * @details
 * - Solo toca la tabla `habits` (marca `updatedAt` & `pendingSync`).
 * - La creación real de la fila `HabitActivityEntity` se hará en la
 *   fase de “actividades”, pero la interfaz pública queda estable.
 *
 * @param id Identificador del hábito.
 * @param at Momento de la acción (por defecto, `Clock.System.now()`).
 */
package com.app.domain.usecase.habit

import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

class MarkCompleted @Inject constructor(
    private val repo: HabitRepository
) { suspend operator fun invoke(id: HabitId, at: Instant = Clock.System.now()) =
    repo.markCompleted(id, at) }