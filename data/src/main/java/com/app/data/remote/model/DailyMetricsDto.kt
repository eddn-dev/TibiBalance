package com.app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyMetricsDto(
    val date   : String,  // “2025-06-17”; ISO-8601 for easy queries
    val steps  : Int,
    val kcal   : Int
)
