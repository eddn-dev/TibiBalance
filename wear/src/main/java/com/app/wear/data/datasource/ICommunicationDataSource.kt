package com.app.wear.data.datasource


import com.app.wear.data.model.DailyMetricsPayload
import com.app.wear.data.model.HabitUpdatePayload

interface ICommunicationDataSource {
    suspend fun sendMetricsPayload(payload: DailyMetricsPayload): Result<Unit>
    suspend fun sendHabitUpdatePayload(payload: HabitUpdatePayload): Result<Unit>
    // Podrías añadir métodos para recibir datos si el wear necesita escuchar activamente
}
