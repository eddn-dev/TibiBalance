package com.app.wear.domain.model

data class WearableDailyMetrics(
    val steps: Int,
    val heartRate: Float?,
    val caloriesBurned: Float?,
    val activeMinutes: Int?,
    val distanceMeters: Float?,
    val timestamp: Long,
    val userId: String?
)
