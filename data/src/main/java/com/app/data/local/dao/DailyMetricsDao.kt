// data/src/main/java/com/app/data/local/dao/DailyMetricsDao.kt
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.DailyMetricsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * @file    DailyMetricsDao.kt
 * @ingroup data_local_dao
 */
@Dao
interface DailyMetricsDao {

    @Query("""
        SELECT * FROM daily_metrics
        WHERE date BETWEEN :from AND :to
        ORDER BY date ASC
    """)
    fun observeRange(from: String, to: String): Flow<List<DailyMetricsEntity>>

    @Upsert
    suspend fun upsertAll(metrics: List<DailyMetricsEntity>)

    @Query("SELECT * FROM daily_metrics WHERE date = :date")
    suspend fun getOneByDate(date: String): DailyMetricsEntity?

    //Borra todos los registros de la tabla
    @Query("DELETE FROM daily_metrics")
    suspend fun clear()

    /**
     * 1) Devuelve *todas* las métricas (Flow), sin filtrar por rango.
     *    (Se usa “String” porque tu entidad sigue teniendo “date: LocalDate” convertido por TypeConverter,
     *     pero en @Query puedes escribirlo como texto para Room. Puedes usar LocalDate si ya tienes el Converter.)
     */
    @Query("SELECT * FROM daily_metrics ORDER BY date ASC")
    fun getAllMetrics(): Flow<List<DailyMetricsEntity>>

    /**
     * 2) Inserta o actualiza una sola métrica.
     *    (En el ZIP original solo tenías upsertAll(List<DailyMetricsEntity>)).
     */
    @Upsert
    suspend fun upsert(entity: DailyMetricsEntity)

    /**
     * 3) Actualiza el flag `pendingSync` para varias fechas (clave primaria “date”).
     *    En tu entidad `DailyMetricsEntity` el campo “date” es de tipo LocalDate (convertido por TypeConverter),
     *    así que aquí lo usamos como parámetro (Room lo convierte internamente).
     */
    @Query("UPDATE daily_metrics SET meta_pendingSync = :flag WHERE date IN (:dates)")
    suspend fun updatePendingSyncFlag(dates: List<LocalDate>, flag: Boolean)

    /**
     * 4) Cuenta cuántas métricas tienen `meta_pendingSync = :flag`.
     *    Necesario para el flow de “countPending()” de tu repositorio.
     */
    @Query("SELECT COUNT(*) FROM daily_metrics WHERE meta_pendingSync = :flag")
    fun countByPendingSync(flag: Boolean): Flow<Int>
}
