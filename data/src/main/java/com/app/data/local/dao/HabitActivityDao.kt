/**
 * @file    HabitActivityDao.kt
 * @ingroup data_local_dao
 * @brief   CRUD para la tabla `activities`.
 */
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.HabitActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitActivityDao {

    @Query("""
        SELECT * FROM activities
        WHERE habitId = :habitId
        ORDER BY completedAt DESC
    """)
    fun observeByHabit(habitId: String): Flow<List<HabitActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: HabitActivityEntity)

    /** Limpia tombstones o filas ya sincronizadas. */
    @Query("""
        DELETE FROM activities
        WHERE meta_deletedAt IS NOT NULL
           OR meta_pendingSync = 0
    """)
    suspend fun purgeSyncedOrDeleted()
}
