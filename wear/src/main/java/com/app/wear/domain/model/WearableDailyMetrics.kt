package com.app.wear.domain.model

// Modelo de dominio puro para las métricas diarias en el wearable.
// No debe tener anotaciones de serialización ni dependencias de otras capas.
data class WearableDailyMetrics(
    val steps: Int,
    val heartRate: Float?, // Puede ser nulo si no está disponible
    val caloriesBurned: Float?, // Puede ser nulo
    val activeMinutes: Int?, // Puede ser nulo
    val distanceMeters: Float?, // Puede ser nulo
    val timestamp: Long, // Momento de la recolección o agregación
    val userId: String? // Opcional: si el wearable está consciente del usuario logueado
)
