package com.app.domain.metrics.entity

import kotlinx.datetime.LocalDate

/**
 * Métricas de actividad del usuario en una fecha dada.
 *
 * @property userId        UID de Firebase Authentication.
 * @property date          Día al que pertenecen las métricas (zona local → UTC-00).
 * @property steps         Pasos totales ese día.
 * @property heartRateAvg  Frecuencia cardiaca promedio en bpm (nullable si no registrada).
 * @property calories      Calorías quemadas en kcal (nullable si no registrada).
 */
data class UserMetrics(
    val userId: String,
    val date: LocalDate,
    val steps: Int,
    val heartRateAvg: Int?,
    val calories: Int?
)
