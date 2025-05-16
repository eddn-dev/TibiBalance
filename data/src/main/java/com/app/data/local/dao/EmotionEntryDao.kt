package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.EmotionEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * @file    EmotionEntryDao.kt
 * @ingroup data_local_dao
 */
@Dao
interface EmotionEntryDao {

    @Query("SELECT * FROM emotions ORDER BY date DESC")
    fun observeAll(): Flow<List<EmotionEntryEntity>>

    @Upsert
    suspend fun upsert(entry: EmotionEntryEntity)
}
