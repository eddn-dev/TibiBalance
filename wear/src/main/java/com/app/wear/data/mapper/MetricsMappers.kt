package com.app.wear.data.mapper

import com.app.wear.data.model.DailyMetricsPayload
import com.app.wear.domain.model.WearableDailyMetrics

// Convierte el modelo de dominio del wearable a un DTO para la transmisión.
fun WearableDailyMetrics.toPayload(): DailyMetricsPayload {
    return DailyMetricsPayload(
        steps = this.steps,
        heartRate = this.heartRate,
        caloriesBurned = this.caloriesBurned,
        activeMinutes = this.activeMinutes,
        distanceMeters = this.distanceMeters,
        userId = this.userId,
        timestamp = this.timestamp
    )
}
