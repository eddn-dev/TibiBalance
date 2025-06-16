package com.app.data.metrics.remote

import kotlinx.datetime.LocalDate

/**
 * DTO para transferencia de métricas de usuario con Firestore.
 */
data class UserMetricsDto(
    val userId: String = "",
    val date: LocalDate,
    val steps: Int = 0,
    val heartRateAvg: Int? = null,
    val calories: Int? = null
)
