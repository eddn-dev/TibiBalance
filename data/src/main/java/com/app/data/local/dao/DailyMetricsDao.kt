/* :data/src/main/kotlin/com/app/data/local/dao/DailyMetricsDao.kt */
package com.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.data.local.entities.DailyMetricsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface DailyMetricsDao {

    @Upsert
    suspend fun upsert(entity: DailyMetricsEntity)

    /** Stream one row; UI stays reactive. */
    @Query(
        "SELECT * FROM daily_metrics WHERE date = :date LIMIT 1"
    )
    fun observeByDate(date: LocalDate): Flow<DailyMetricsEntity?>

    @Query("SELECT * FROM daily_metrics WHERE date BETWEEN :start AND :end")
    fun observeRange(start: LocalDate, end: LocalDate): Flow<List<DailyMetricsEntity>>

    @Query("DELETE FROM daily_metrics WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: LocalDate)

}
