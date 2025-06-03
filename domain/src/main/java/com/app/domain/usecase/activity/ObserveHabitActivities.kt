/**
 * @file    ObserveHabitActivities.kt
 * @ingroup domain_usecase_activity
 * @brief   Historial completo de un hábito.
 */
package com.app.domain.usecase.activity

import com.app.domain.entities.HabitActivity
import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHabitActivities @Inject constructor(
    private val repo: HabitActivityRepository
) {
    operator fun invoke(habitId: HabitId): Flow<List<HabitActivity>> =
        repo.observeByHabit(habitId)
}
