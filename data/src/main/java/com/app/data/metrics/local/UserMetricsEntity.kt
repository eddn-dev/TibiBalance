package com.app.data.metrics.local

import androidx.room.Entity
import androidx.room.Index
import kotlinx.datetime.LocalDate

/**
 * Entidad de Room para almacenar las métricas de usuario.
 */
@Entity(
    tableName = "user_metrics",
    primaryKeys = ["userId", "date"],
    indices = [Index("date")]
)
data class UserMetricsEntity(
    val userId: String,
    val date: LocalDate,
    val steps: Int,
    val heartRateAvg: Int?,
    val calories: Int?
)
