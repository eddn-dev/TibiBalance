package com.app.data.metrics.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * DAO para operaciones CRUD sobre UserMetricsEntity.
 */
@Dao
interface UserMetricsDao {

    /** Inserta o reemplaza un listado de métricas. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<UserMetricsEntity>)

    /** Actualiza una métrica existente. */
    @Update
    suspend fun update(entity: UserMetricsEntity)

    /** Elimina las métricas de la fecha indicada. */
    @Query("DELETE FROM user_metrics WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)

    /**
     * Observa las métricas entre dos fechas (inclusive), ordenadas de más reciente a más antigua.
     */
    @Query("""
        SELECT * FROM user_metrics
        WHERE date BETWEEN :from AND :to
        ORDER BY date DESC
    """)
    fun observe(from: LocalDate, to: LocalDate): Flow<List<UserMetricsEntity>>
}
