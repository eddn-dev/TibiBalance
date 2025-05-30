package com.app.wear.domain.repository

import com.app.wear.domain.model.WearableDailyMetrics
import kotlinx.coroutines.flow.Flow

// Interfaz para el repositorio que maneja las métricas del wearable.
interface IWearMetricsRepository {
    // Envía las métricas recolectadas al dispositivo móvil (app).
    // Devuelve un Result para indicar éxito o fallo.
    suspend fun sendMetricsToCompanionApp(metrics: WearableDailyMetrics): Result<Unit>

    // Obtiene un flujo de datos de ritmo cardíaco en tiempo real desde los sensores.
    fun getRealTimeHeartRate(): Flow<Int> // Emite el valor del HR

    // Obtiene el conteo de pasos actual (podría ser un valor único o un flujo).
    suspend fun getCurrentSteps(): Int

    // (Opcional) Si el wearable necesita obtener métricas históricas (poco común para wear)
    // fun getHistoricalMetrics(from: Long, to: Long): Flow<List<WearableDailyMetrics>>
}
