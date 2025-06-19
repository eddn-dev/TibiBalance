/* RefreshActivitiesStatusForDate.kt */
package com.app.domain.usecase.activity

import com.app.domain.repository.HabitActivityRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class RefreshActivitiesStatusForDate @Inject constructor(
    private val actRepo: HabitActivityRepository
) {
    /**
     * Actualiza los estados (PENDING → AVAILABLE / MISSED) de todas las actividades
     * correspondientes a la `date` indicada.
     *
     * @param date Día a refrescar (normalmente hoy).
     * @param now  Momento de referencia; default = Clock.System.now().
     */
    suspend operator fun invoke(
        date: LocalDate,
        now : Instant = Clock.System.now()
    ) = actRepo.refreshStatusesForDate(date, now)
}
