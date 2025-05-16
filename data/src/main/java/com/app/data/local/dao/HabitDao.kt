package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

/**
 * @file    HabitDao.kt
 * @ingroup data_local_dao
 * @brief   Acceso a datos para la tabla `habits`.
 */
@Dao
interface HabitDao {

    /** Devuelve todos los hábitos ordenados por categoría y nombre. */
    @Query("""
        SELECT * FROM habits
        ORDER BY category, name COLLATE NOCASE
    """)
    fun observeAll(): Flow<List<HabitEntity>>

    /** Devuelve un hábito por ID. */
    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<HabitEntity?>

    /** Inserta o actualiza en bloque. */
    @Upsert
    suspend fun upsertAll(vararg habits: HabitEntity)

    /** Borra un hábito (onDelete CASCADE elimina también actividades). */
    @Delete
    suspend fun delete(entity: HabitEntity)

    /** Marca `deletedAt` y `pendingSync` para borrado offline-first. */
    @Query("""
        UPDATE habits
        SET meta_deletedAt = :epochMs,
            meta_pendingSync = 1
        WHERE id = :id
    """)
    suspend fun softDelete(id: String, epochMs: Long)
}
