/**
 * @file    GetHabitsFlowUseCase.kt
 * @ingroup domain_usecase_habit
 * @brief   Expone un flujo reactivo de hábitos ordenados por categoría y nombre.
 *
 * @details
 * Este caso de uso delega en [HabitRepository.getHabitsFlow] y no realiza
 * transformación alguna: la UI decide cómo cachear o agrupar los resultados.
 *
 * @author  Edd
 */
package com.app.domain.usecase.habit

import com.app.domain.entities.Habit
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitsFlow @Inject constructor(
    private val repo: HabitRepository
) { operator fun invoke() = repo.observeUserHabits() }
