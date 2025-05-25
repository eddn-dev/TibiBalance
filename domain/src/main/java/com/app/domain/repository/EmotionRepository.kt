/**
 * @file    EmotionRepository.kt
 * @ingroup domain_repository
 * @brief   Contrato de operaciones offline-first para emociones.
 */
package com.app.domain.repository

import com.app.domain.entities.EmotionEntry
import kotlinx.coroutines.flow.Flow

interface EmotionRepository {

    /** Devuelve un flujo con **todos** los registros ordenados por fecha desc. */
    fun observeAll(): Flow<List<EmotionEntry>>

    /** Inserta o actualiza la emoción de un día (último estado sobrescribe). */
    suspend fun upsert(entry: EmotionEntry)
    suspend fun syncNow(): Result<Unit>
}
