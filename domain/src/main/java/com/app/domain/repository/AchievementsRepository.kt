package com.app.domain.repository

interface AchievementsRepository {
    suspend fun initializeAchievementsIfMissing(userId: String)
    suspend fun unlockIfNotYet(userId: String, achievementId: String): Boolean
    suspend fun updateProgress(userId: String, achievementId: String, progress: Int)
}
