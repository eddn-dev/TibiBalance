package com.app.domain.metrics.repository

import com.app.domain.metrics.entity.UserMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Contrato para operaciones de métricas de usuario.
 */
interface UserMetricsRepository {

    /** Inserta o sincroniza un lote de métricas (local + remoto). */
    suspend fun sync(metrics: List<UserMetrics>)

    /** Actualiza una métrica individual en local + remoto. */
    suspend fun update(metric: UserMetrics)

    /** Elimina todas las métricas de la fecha indicada. */
    suspend fun deleteByDate(date: LocalDate)

    /** Observa los registros de métricas entre dos fechas (inclusive). */
    fun observe(from: LocalDate, to: LocalDate): Flow<List<UserMetrics>>
}
