/**
 * @file    HabitRemoteDataSource.kt
 * @ingroup data_remote_datasource
 * @brief   Contrato CRUD para Firestore aislado de Room y del repo.
 */
package com.app.data.remote.datasource

import com.app.data.remote.model.HabitDto
import kotlinx.coroutines.flow.Flow

interface HabitRemoteDataSource {

    /** Observa cambios remotos en tiempo real. */
    fun listenHabits(): Flow<HabitDto>

    /** Sube (merge) un hábito; sobreescribe por _doc id_. */
    suspend fun pushHabit(dto: HabitDto)

    /** Devuelve dump completo de colección `habits/`. */
    suspend fun pullHabits(): List<HabitDto>

    /** Borra documento remoto (hard-delete). */
    suspend fun deleteHabit(id: String)
}