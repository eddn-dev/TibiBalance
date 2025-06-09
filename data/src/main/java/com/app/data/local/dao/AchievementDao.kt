package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.AchievementEntity
import com.app.domain.ids.AchievementId
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    /* ---- observación ---- */
    @Query("SELECT * FROM achievements")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AchievementEntity?>

    /* ---- lectura puntual ---- */
    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): AchievementEntity?

    /* ---- inserción / reemplazo ---- */
    @Upsert
    suspend fun upsert(entity: AchievementEntity)

    @Upsert
    suspend fun upsert(entities: List<AchievementEntity>)

    // AchievementDao.kt
    @Query("SELECT id FROM achievements")
    suspend fun allIds(): List<String>

    /* ---- pendientes de sincronizar ---- */
    @Query("SELECT * FROM achievements WHERE meta_pendingSync = 1")
    suspend fun pendingToSync(): List<AchievementEntity>
}
