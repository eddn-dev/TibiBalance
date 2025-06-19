package com.app.domain.repository

import com.app.domain.entities.HabitActivity
import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Repositorio offline-first para [HabitActivity].
 *
 *  • Room  → fuente reactiva y caché local.
 *  • Firestore → sincronización (colección: users/{uid}/habitActivities).
 */
interface HabitActivityRepository {

    /* ---------- observación reactiva ---------- */

    /** Actividades de un día (pantalla principal). */
    fun observeByDate(date: LocalDate): Flow<List<HabitActivity>>

    /** Historial de un hábito concreto. */
    fun observeByHabit(habitId: HabitId): Flow<List<HabitActivity>>

    /* ---------- creación / lote ---------- */

    /** Inserta (o ignora si existe). Útil al generar actividades. */
    suspend fun insert(entity: HabitActivity)

    /** Inserta en bloque (OnConflictStrategy.IGNORE). */
    suspend fun insertAll(entities: List<HabitActivity>)

    /* ---------- progreso / actualización ---------- */

    /** Actualiza todo el objeto (incluye status, recordedQty, loggedAt…). */
    suspend fun update(entity: HabitActivity)

    /**
     * Helper para registrar progreso.
     * @param recordedQty  Nueva cantidad (puede ser null si no aplica).
     * @param newStatus    Estado resultante (COMPLETED, PARTIALLY_COMPLETED, MISSED).
     * @param loggedAtUtc  Momento del registro (UTC).
     */
    suspend fun markProgress(
        id: ActivityId,
        recordedQty: Int?,
        newStatus: ActivityStatus,
        loggedAtUtc: kotlinx.datetime.Instant
    )

    /** Este helper ayuda a determinar huecos existentes para la fecha **/
    suspend fun countByHabitAndDate(habitId: HabitId, date: LocalDate): Int

    /** Recalcula y actualiza status de todas las actividades de una fecha. */
    suspend fun refreshStatusesForDate(date: LocalDate, now: Instant = Clock.System.now())

    /* ---------- limpieza ---------- */

    /** Borra una actividad (por cambios en el hábito o depuración). */
    suspend fun delete(id: ActivityId)

    /** Purga pendings sincronizados o tombstones. */
    suspend fun purgeSyncedOrDeleted()

    /* ---------- sincronización manual ---------- */

    /** Fuerza push/pull con Firestore (worker o «tirar hacia abajo»). */
    suspend fun syncNow(): Result<Unit>
}
