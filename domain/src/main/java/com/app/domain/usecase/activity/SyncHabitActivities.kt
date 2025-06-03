/**
 * @file    SyncHabitActivities.kt
 * @ingroup domain_usecase_activity
 * @brief   Fuerza push/pull inmediato con Firestore.
 */
package com.app.domain.usecase.activity

import com.app.domain.repository.HabitActivityRepository
import javax.inject.Inject

class SyncHabitActivities @Inject constructor(
    private val repo: HabitActivityRepository
) {
    suspend operator fun invoke() = repo.syncNow()
}
