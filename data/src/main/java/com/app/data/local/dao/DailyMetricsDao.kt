// data/src/main/java/com/app/data/local/dao/DailyMetricsDao.kt
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.DailyMetricsEntity
import kotlinx.coroutines.flow.Flow

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
}
