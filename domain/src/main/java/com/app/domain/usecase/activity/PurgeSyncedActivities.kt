/**
 * @file    PurgeSyncedActivities.kt
 * @ingroup domain_usecase_activity
 * @brief   Elimina tombstones y locales ya sincronizados.
 */
package com.app.domain.usecase.activity

import com.app.domain.repository.HabitActivityRepository
import javax.inject.Inject

class PurgeSyncedActivities @Inject constructor(
    private val repo: HabitActivityRepository
) {
    suspend operator fun invoke() =
        repo.purgeSyncedOrDeleted()
}
