/**
 * @file    CreateHabitUseCase.kt
 * @ingroup domain_usecase_habit
 * @brief   Crea un nuevo hábito localmente y lo marca para sincronización.
 *
 * @details
 * - Asume que el [Habit] ya contiene un [HabitId] único (UUID u otro).
 * - Rellena campos `meta.pendingSync=true` y `meta.createdAt/updatedAt=now`
 *   dentro del repositorio, por lo que **no es necesario** tocar el modelo aquí.
 *
 * @return  [Result.success] cuando la inserción local fue exitosa.
 *
 * @see HabitRepository.createHabit
 */
package com.app.domain.usecase.habit

import com.app.domain.entities.Habit
import com.app.domain.repository.HabitRepository
import javax.inject.Inject

class CreateHabitUseCase @Inject constructor(
    private val repo: HabitRepository
) {
    /** @param habit Entidad de dominio lista para guardarse. */
    suspend operator fun invoke(habit: Habit): Result<Unit> =
        repo.createHabit(habit)
}
