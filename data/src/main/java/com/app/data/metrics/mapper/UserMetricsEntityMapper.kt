package com.app.data.metrics.mapper

import com.app.data.metrics.local.UserMetricsEntity
import com.app.domain.metrics.entity.UserMetrics
import kotlinx.datetime.LocalDate

/** Convierte entidad de Room → modelo de dominio */
fun UserMetricsEntity.toDomain(): UserMetrics =
    UserMetrics(
        userId = userId,
        date = date,
        steps = steps,
        heartRateAvg = heartRateAvg,
        calories = calories
    )

/** Convierte modelo de dominio → entidad de Room */
fun UserMetrics.toEntity(): UserMetricsEntity =
    UserMetricsEntity(
        userId = userId,
        date = date,
        steps = steps,
        heartRateAvg = heartRateAvg,
        calories = calories
    )
