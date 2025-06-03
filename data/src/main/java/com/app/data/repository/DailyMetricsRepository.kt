package com.app.data.repository

import com.app.data.local.dao.DailyMetricsDao
import com.app.data.local.entities.DailyMetricsEntity
import com.app.data.remote.model.DailyMetricsPayload
import com.app.domain.common.SyncMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class DailyMetricsRepository @Inject constructor(
    private val dailyMetricsDao: DailyMetricsDao
) {
    // 1) Guardar en Room a partir de un payload Wear (DailyMetricsPayload)
    suspend fun saveFromPayload(payload: DailyMetricsPayload) {
        val dataInstant: Instant =
            Instant.fromEpochMilliseconds(payload.timestamp)
        val localDate: LocalDate =
            dataInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        val entity = DailyMetricsEntity(
            date       = localDate,
            steps      = payload.steps,
            avgHeart   = payload.heartRate?.toInt(),
            calories   = payload.caloriesBurned?.toInt(),
            source     = "wear",
            importedAt = dataInstant,
            meta       = SyncMeta(
                createdAt   = Clock.System.now(),  // <— aquí
                updatedAt   = Clock.System.now(),  // <— y aquí
                deletedAt   = null,
                pendingSync = true
            )
        )
        dailyMetricsDao.upsertAll(listOf(entity))
    }

    // 2) Obtener solo las entidades pendientes de sincronizar (meta.pendingSync = true)
    suspend fun getPendingSyncEntities(): List<DailyMetricsEntity> {
        val today: LocalDate =
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val from  = LocalDate(1970, 1, 1).toString()
        val to    = today.toString()
        val allInRange = dailyMetricsDao.observeRange(from, to).first()
        return allInRange.filter { it.meta.pendingSync }
    }

    // 3) Marcar como sincronizadas (pendingSync = false) ciertas fechas
    suspend fun markAsSynced(dates: List<LocalDate>) {
        val now = Clock.System.now()  // <— aquí definimos “now”
        val toUpsert = dates.mapNotNull { date ->
            val existing = dailyMetricsDao.getOneByDate(date.toString())
                ?: return@mapNotNull null
            existing.copy(
                meta = existing.meta.copy(
                    updatedAt   = now,         // <— aquí usamos esa “now”
                    pendingSync = false
                )
            )
        }
        if (toUpsert.isNotEmpty()) {
            dailyMetricsDao.upsertAll(toUpsert)
        }
    }

    // 4) Metodo para observar métricas en un rango (para UI)
    fun observeRange(from: String, to: String): Flow<List<DailyMetricsEntity>> {
        return dailyMetricsDao.observeRange(from, to)
    }
}
