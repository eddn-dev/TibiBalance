package com.app.wear.data.datasource

import kotlinx.coroutines.flow.Flow

interface ISensorDataSource {
    fun getHeartRateUpdates(): Flow<Int> // Flujo de valores de HR
    suspend fun fetchCurrentStepCount(): Int // Valor único de pasos
    // Podrías añadir otros como activeMinutes, distance, etc.
}
