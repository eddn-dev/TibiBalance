/**
 * @file    HabitRepository.kt
 * @ingroup domain_repository
 * @brief   Contrato offline-first para acceder y modificar los hábitos del usuario.
 *
 * @details
 * Este repositorio abstrae la orquestación entre la caché cifrada **Room** y
 * la fuente remota **Cloud Firestore**:
 *
 *  • Expone un flujo reactivo de [Habit] que fusiona cambios locales y remotos.
 *  • Aplica mutaciones optimistas en disco (`pendingSync=true`) y delega la subida
 *    al `SyncWorker`.
 *  • Resuelve conflictos por política _Last-Write-Wins_ usando `SyncMeta.updatedAt`.
 *  • Gestiona *tombstones* escribiendo `deletedAt` en lugar de borrar filas para
 *    preservar coherencia entre dispositivos.
 *
 * Todas las operaciones suspenden y devuelven [Result] para que la UI decida
 * reintentar o mostrar errores.  Ninguna función lanza excepciones crudas.
 *
 * @see com.app.domain.entities.Habit
 * @see com.app.domain.common.SyncMeta
 */
package com.app.domain.repository

import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface HabitRepository {

    /** Flujo en caliente (Room + Firestore) ordenado por categoría y nombre. */
    fun getHabitsFlow(): Flow<List<Habit>>

    /** Crea un hábito nuevo con `pendingSync=true` (optimista). */
    suspend fun createHabit(habit: Habit): Result<Unit>

    /**
     * Actualiza un hábito.
     * 1. Escribe en Room.
     * 2. Marca `pendingSync=true`.
     * 3. Refresca `updatedAt = Clock.System.now()`.
     */
    suspend fun updateHabit(habit: Habit): Result<Unit>

    /**
     * Borra un hábito.
     * Cuando [hard] es `false` (por defecto) se deja *tombstone* (`deletedAt`).
     * Un hard-delete sólo procede si el documento jamás se subió a Firestore.
     */
    suspend fun deleteHabit(
        id: HabitId,
        hard: Boolean = false
    ): Result<Unit>

    /**
     * Registra una finalización de sesión (ActivityType.COMPLETE) para [id].
     * También puede usarse para marcar skip/reset, según lógica de UI.
     */
    suspend fun markCompleted(
        id: HabitId,
        at: Instant = Clock.System.now(),
    ): Result<Unit>

    /**
     * Fuerza reconciliación inmediata Room ↔ Firestore.
     * Normalmente invocado por el botón “Sincronizar ahora” o por pruebas.
     */
    suspend fun syncNow(): Result<Unit>
}
