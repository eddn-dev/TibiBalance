package com.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.data.local.entities.HabitTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitTemplateDao {
    @Query("SELECT * FROM habit_templates ORDER BY category, name")
    fun observeAll(): Flow<List<HabitTemplateEntity>>

    @Upsert suspend fun upsert(templates: List<HabitTemplateEntity>)
}