package com.app.wear.data.datasource

import kotlinx.coroutines.flow.Flow

interface ISensorDataSource {
    fun getHeartRateUpdates(): Flow<Int>
    suspend fun fetchCurrentStepCount(): Int
}
