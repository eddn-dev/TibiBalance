/**
 * @file    DailyTipsRepository.kt
 * @ingroup domain_repository
 * @brief   Contrato Clean Architecture para tips diarios.
 *
 * @note  *No* se implementa aquí; la implementación concreta vive en **:data**.
 */
package com.app.domain.repository

import com.app.domain.entities.DailyTip
import kotlinx.coroutines.flow.Flow

interface DailyTipsRepository {
    /** Flujo con el tip que debe mostrarse hoy (o null). */
    fun todayTip(): Flow<DailyTip?>

    /** Fuerza descarga remota y persistencia local. */
    suspend fun refresh(): Result<Unit>

    /** Marca que el tip de hoy ya se mostró (para no repetir). */
    suspend fun markAsShown(id: Int)
}
