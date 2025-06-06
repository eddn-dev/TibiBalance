package com.app.data.repository

import com.app.data.local.dao.DailyMetricsDao
import com.app.data.local.entities.DailyMetricsEntity
import com.app.domain.common.SyncMeta
import com.app.domain.entities.DailyMetrics
import com.app.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de MetricsRepository que usa Room (DailyMetricsDao) para persistir métricas.
 */
@Singleton
class MetricsRepositoryImpl @Inject constructor(
    private val dao: DailyMetricsDao
) : MetricsRepository {

    /**
     * Retorna un Flow de todas las métricas en la base local,
     * convirtiendo cada entidad Room a modelo de dominio.
     */
    override fun streamDailyMetrics(): Flow<List<DailyMetrics>> {
        return dao.getAllMetrics()
            .map { listEntities ->
                listEntities.map { entity ->
                    entity.toDomainModel()
                }
            }
    }

    /**
     * Inserta o actualiza una métrica (DailyMetrics) en Room, marcándola con pendingSync = true.
     */
    override suspend fun upsert(metrics: DailyMetrics) {
        val entity = metrics.toEntity()
        dao.upsert(entity)
    }

    /**
     * Marca como sincronizadas (pendingSync = false) las métricas cuyas fechas (LocalDate) se pasen aquí.
     */
    override suspend fun markSynced(dates: List<LocalDate>) {
        dao.updatePendingSyncFlag(dates, false)
    }

    /**
     * Devuelve un Flow con la cantidad de métricas que aún tienen pendingSync = true.
     */
    override fun countPending(): Flow<Int> {
        return dao.countByPendingSync(true)
    }

    // ——— Funciones auxiliares de mapeo Entity ↔ Domain ———

    /**
     * Convierte una entidad Room (DailyMetricsEntity) a modelo de dominio (DailyMetrics).
     */
    private fun DailyMetricsEntity.toDomainModel(): DailyMetrics {
        return DailyMetrics(
            date       = this.date,
            steps      = this.steps,
            avgHeart   = this.avgHeart,
            calories   = this.calories,
            source     = this.source,
            importedAt = this.importedAt,
            meta       = this.meta
        )
    }

    /**
     * Convierte un modelo de dominio (DailyMetrics) a entidad Room (DailyMetricsEntity).
     * Al archivar la entidad, asignamos createdAt y updatedAt igual que importedAt,
     * y dejamos pendingSync = true para que el Worker la suba más tarde.
     */
    private fun DailyMetrics.toEntity(): DailyMetricsEntity {
        return DailyMetricsEntity(
            date       = this.date,
            steps      = this.steps,
            avgHeart   = this.avgHeart,
            calories   = this.calories,
            source     = this.source,
            importedAt = this.importedAt,
            meta       = SyncMeta(
                pendingSync = true,
                createdAt   = this.importedAt,
                updatedAt   = this.importedAt
            )
        )
    }
}
