/**
 * @file    HabitDao.kt
 * @ingroup data_local_dao
 * @brief   CRUD + helpers para sincronización de la tabla `habits`.
 */
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.HabitActivityEntity
import com.app.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isBuiltIn = :builtIn ORDER BY name")
    fun observeByBuiltIn(builtIn: Boolean): Flow<List<HabitEntity>>

    @Upsert
    suspend fun upsert(entity: HabitEntity)

    @Upsert
    suspend fun upsert(entities: List<HabitEntity>)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM habits WHERE m_pendingSync = 1")
    suspend fun pendingToSync(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): HabitEntity?

    /* ─────────── Actividades asociadas ─────────────────────────── */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(entity: HabitActivityEntity)
}
