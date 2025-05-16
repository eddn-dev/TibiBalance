/**
 * @file    DailyMetricsMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión DailyMetricsEntity ↔ DailyMetrics (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.DailyMetricsEntity
import com.app.domain.entities.DailyMetrics

object DailyMetricsMappers {

    fun DailyMetricsEntity.toDomain(): DailyMetrics = DailyMetrics(
        date       = date,
        steps      = steps,
        avgHeart   = avgHeart,
        calories   = calories,
        source     = source,
        importedAt = importedAt,
        meta       = meta
    )

    fun DailyMetrics.toEntity(): DailyMetricsEntity = DailyMetricsEntity(
        date       = date,
        steps      = steps,
        avgHeart   = avgHeart,
        calories   = calories,
        source     = source,
        importedAt = importedAt,
        meta       = meta
    )
}
