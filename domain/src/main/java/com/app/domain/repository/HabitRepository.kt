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

    /* Flujos ------------------------------ */
    fun observeUserHabits()     : Flow<List<Habit>>   // isBuiltIn = false
    fun observeSuggestedHabits(): Flow<List<Habit>>   // isBuiltIn = true
    fun observeHabit(id: HabitId) : Flow<Habit?>

    /* Comandos ---------------------------- */
    suspend fun create(habit: Habit)
    suspend fun update(habit: Habit)
    suspend fun delete(id: HabitId)
    suspend fun markCompleted(id: HabitId, at: Instant = Clock.System.now())
    suspend fun syncNow(): Result<Unit>
}
