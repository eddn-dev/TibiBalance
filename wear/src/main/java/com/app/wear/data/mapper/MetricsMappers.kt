package com.app.wear.data.mapper

import com.app.wear.data.model.DailyMetricsPayload
import com.app.wear.domain.model.WearableDailyMetrics

fun WearableDailyMetrics.toPayload(): DailyMetricsPayload = DailyMetricsPayload(
    steps = steps,
    heartRate = heartRate,
    caloriesBurned = caloriesBurned,
    activeMinutes = activeMinutes,
    distanceMeters = distanceMeters,
    userId = userId,
    timestamp = timestamp
)
