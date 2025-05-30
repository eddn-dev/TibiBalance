package com.app.wear.data.model // Asegúrate que el paquete sea correcto

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
