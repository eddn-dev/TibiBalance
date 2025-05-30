package com.app.wear.domain.repository

import com.app.wear.domain.model.WearableDailyMetrics
import kotlinx.coroutines.flow.Flow

interface IWearMetricsRepository {
    suspend fun sendMetricsToCompanionApp(metrics: WearableDailyMetrics): Result<Unit>
    fun getRealTimeHeartRate(): Flow<Int>
    suspend fun getCurrentSteps(): Int
}
