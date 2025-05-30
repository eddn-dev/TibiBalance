package com.app.wear.domain.repository

import com.app.wear.domain.model.WearableHabitInfo
import kotlinx.coroutines.flow.Flow

interface IWearHabitRepository {
    fun getObservableHabits(): Flow<List<WearableHabitInfo>>
    suspend fun toggleHabitCompletion(habitId: String, isCompleted: Boolean): Result<Unit>
}
