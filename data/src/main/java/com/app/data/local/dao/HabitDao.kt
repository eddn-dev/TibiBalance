/**
 * @file    HabitDao.kt
 * @ingroup data_local_dao
 * @brief   CRUD + helpers para sincronización de la tabla `habits`.
 */
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    /* ─── Lectura reactiva ─────────────────────────── */
    @Query("""
        SELECT * FROM habits
        ORDER BY category, name COLLATE NOCASE
    """)
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<HabitEntity?>

    /* ─── Upsert ───────────────────────────────────── */
    @Upsert suspend fun upsertAll(vararg habits: HabitEntity)

    /* ─── Borrado ──────────────────────────────────── */
    @Delete suspend fun delete(entity: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        UPDATE habits
        SET meta_deletedAt  = :epochMs,
            meta_pendingSync = 1
        WHERE id = :id
    """)
    suspend fun softDelete(id: String, epochMs: Long)

    /* ─── Sincronización ──────────────────────────── */
    @Query("SELECT * FROM habits WHERE meta_pendingSync = 1")
    suspend fun pendingSyncSnapshot(): List<HabitEntity>

    @Query("""
        UPDATE habits
        SET meta_pendingSync = 0
        WHERE id = :id
    """)
    suspend fun clearPending(id: String)

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun findByIdSync(id: String): HabitEntity?

    /* ─── Helper para COMPLETE / SKIP / RESET ─────── */
    @Query("""
        UPDATE habits
        SET meta_updatedAt  = :epochMs,
            meta_pendingSync = 1
        WHERE id = :id
    """)
    suspend fun touchForCompletion(id: String, epochMs: Long)
}
