/**
 * @file    HabitActivityDao.kt
 * @ingroup data_local_dao
 * @brief   CRUD + flujos reactivos para la tabla `activities`.
 *
 *  ▸  Conversores Room permiten LocalDate / LocalTime.
 *  ▸  “NULL LAST” con `CASE …`.
 *  ▸  Nuevos helpers usados por el repositorio:
 *        • insertAll()
 *        • findById()
 *        • pendingToSync()
 *        • delete(id)
 */
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.HabitActivityEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HabitActivityDao {

    /* ─────────── OBSERVACIÓN ─────────── */

    @Query(
        """
        SELECT * FROM activities
        WHERE habitId = :habitId
        ORDER BY activityDate DESC,
                 CASE WHEN scheduledTime IS NULL THEN 1 ELSE 0 END,
                 scheduledTime
        """
    )
    fun observeByHabit(habitId: String): Flow<List<HabitActivityEntity>>

    @Query(
        """
            SELECT COUNT(*) FROM activities
            WHERE habitId = :habitId AND activityDate = :date
        """
    )
    suspend fun countByHabitAndDate(habitId: String, date: LocalDate): Int


    @Query(
        """
        SELECT * FROM activities
        WHERE activityDate = :date
        ORDER BY CASE WHEN scheduledTime IS NULL THEN 1 ELSE 0 END,
                 scheduledTime
        """
    )
    fun observeByDate(date: LocalDate): Flow<List<HabitActivityEntity>>

    /* HabitActivityDao.kt */
    @Query("SELECT * FROM activities WHERE activityDate = :date")
    suspend fun listByDate(date: LocalDate): List<HabitActivityEntity>


    /* ────────────── CRUD ────────────── */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: HabitActivityEntity)

    /** Inserta en lote (IGNORE).  */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<HabitActivityEntity>)

    /** REPLACE (upsert) una sola fila. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HabitActivityEntity)

    @Update
    suspend fun update(entity: HabitActivityEntity)

    /** Borrar una fila por ID (soft-delete ya aplicado en dominio si procede). */
    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun delete(id: String)

    /** Devuelve la fila (o null) según ID. */
    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): HabitActivityEntity?

    /** Filas pendientes de sincronizar (`pendingSync = 1`). */
    @Query("SELECT * FROM activities WHERE m_pendingSync = 1")
    suspend fun pendingToSync(): List<HabitActivityEntity>

    /* ────────── MANTENIMIENTO ────────── */

    @Query(
        """
        DELETE FROM activities
        WHERE m_deletedAt IS NOT NULL
           OR m_pendingSync = 0
        """
    )
    suspend fun purgeSyncedOrDeleted()

    @Query("DELETE FROM activities")
    suspend fun clear()
}
