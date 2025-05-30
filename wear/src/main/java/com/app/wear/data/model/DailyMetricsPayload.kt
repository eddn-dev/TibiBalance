package com.app.wear.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyMetricsPayload(
    val steps: Int,
    val heartRate: Float?,
    val caloriesBurned: Float?,
    val activeMinutes: Int?,
    val distanceMeters: Float?,
    val userId: String?,
    val timestamp: Long
)
