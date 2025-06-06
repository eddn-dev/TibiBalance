package com.app.domain.repository

import com.app.domain.entities.DailyMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Contrato del repositorio de métricas diarias.
 * Define las operaciones que la capa de dominio puede realizar sobre las métricas.
 */
interface MetricsRepository {

    /**
     * Devuelve un flujo que emite la lista completa de métricas
     * almacenadas localmente en Room (DailyMetricsEntity mapeadas a DailyMetrics).
     */
    fun streamDailyMetrics(): Flow<List<DailyMetrics>>

    /**
     * Inserta o actualiza una métrica en la base local (Room).
     * Al insertar nueva, quedará con pendingSync = true.
     */
    suspend fun upsert(metrics: DailyMetrics)

    /**
     * Marca como sincronizadas (pendingSync = false) las métricas cuyas fechas
     * (LocalDate) se pasen aquí.
     * Se usa esta función cuando el Worker confirma que las subió a Firestore.
     */
    suspend fun markSynced(dates: List<LocalDate>)

    /**
     * Devuelve un flujo con el número de métricas que aún tienen pendingSync = true.
     * Útil para la UI de estado de sincronización.
     */
    fun countPending(): Flow<Int>
}
