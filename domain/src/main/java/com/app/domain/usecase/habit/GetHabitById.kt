package com.app.domain.usecase.habit

/**
 * @file    GetHabitById.kt
 * @ingroup domain_usecase_habit
 * @brief   Devuelve un flujo reactivo del hábito solicitado.
 *
 * @details
 * - Es *cold*: el repositorio no hace trabajo hasta que alguien se suscribe.
 * - Emite `null` si el hábito se elimina o aún no existe en la base local.
 * - No realiza transformación adicional; la UI decide cómo tratar valores nulos.
 *
 * @author  Edd
 */

import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitById @Inject constructor(
    private val repo: HabitRepository
) {
    /**
     * @param id Identificador único ([HabitId]) del hábito.
     * @return   [Flow] que emite el [Habit] correspondiente o `null`.
     */
    operator fun invoke(id: HabitId): Flow<Habit?> =
        repo.observeHabit(id)
}
