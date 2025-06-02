/**
 * @file    RegisterActivityProgress.kt
 * @ingroup domain_usecase_activity
 * @brief   Actualiza estado y cantidad registrada de una actividad.
 */
package com.app.domain.usecase.activity

import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.repository.HabitActivityRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

class RegisterActivityProgress @Inject constructor(
    private val repo: HabitActivityRepository
) {
    /**
     * @param id          Identificador de la actividad.
     * @param qty         Cantidad registrada (null si no aplica).
     * @param newStatus   COMPLETED / PARTIALLY_COMPLETED / MISSED.
     * @param at          Momento de registro.  *Default ➜ ahora UTC*.
     */
    suspend operator fun invoke(
        id       : ActivityId,
        qty      : Int?,
        newStatus: ActivityStatus,
        at       : Instant = Clock.System.now()
    ) = repo.markProgress(id, qty, newStatus, at)
}
