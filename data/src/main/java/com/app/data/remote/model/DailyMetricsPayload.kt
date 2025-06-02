package com.app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyMetricsPayload(
    val steps: Int,
    val heartRate: Float?,
    val caloriesBurned: Float?,
    val activeMinutes: Int?,
    val distanceMeters: Float?,
    val userId: String?, // Para identificar al usuario en el backend/app móvil
    val timestamp: Long // Unix timestamp en milisegundos
)