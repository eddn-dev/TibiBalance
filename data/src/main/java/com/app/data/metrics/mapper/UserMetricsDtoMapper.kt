package com.app.data.metrics.mapper

import com.app.data.metrics.remote.UserMetricsDto
import com.app.domain.metrics.entity.UserMetrics
import kotlinx.datetime.LocalDate

/** Convierte DTO Firestore → modelo de dominio */
fun UserMetricsDto.toDomain(): UserMetrics =
    UserMetrics(
        userId = userId,
        date = date,
        steps = steps,
        heartRateAvg = heartRateAvg,
        calories = calories
    )

/** Convierte modelo de dominio → DTO Firestore */
fun UserMetrics.toDto(): UserMetricsDto =
    UserMetricsDto(
        userId = userId,
        date = date,
        steps = steps,
        heartRateAvg = heartRateAvg,
        calories = calories
    )
