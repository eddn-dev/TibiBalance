/**
 * @file    ObserveActivitiesByDate.kt
 * @ingroup domain_usecase_activity
 * @brief   Flujo reactivo de actividades para un día concreto.
 */
package com.app.domain.usecase.activity

import com.app.domain.entities.HabitActivity
import com.app.domain.repository.HabitActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ObserveActivitiesByDate @Inject constructor(
    private val repo: HabitActivityRepository
) {
    /** Devuelve un `Flow` siempre actualizado. */
    operator fun invoke(date: LocalDate): Flow<List<HabitActivity>> =
        repo.observeByDate(date)
}
