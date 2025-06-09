package com.app.domain.repository

import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId
import kotlinx.coroutines.flow.Flow

interface AchievementsRepository {

    /* reactivo */
    fun observeAll() : Flow<List<Achievement>>

    /* CRUD offline-first */
    suspend fun upsert(achievement: Achievement)
    suspend fun find(id: AchievementId): Achievement?

    /* progreso */
    suspend fun updateProgress(id: AchievementId, progress: Int)

    /* sync */
    suspend fun syncNow(): Result<Unit>

    /* seeding */
    suspend fun initializeDefaults()
}
