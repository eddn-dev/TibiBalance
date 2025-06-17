package com.app.domain.entities

import kotlinx.datetime.LocalDate

/** Aggregate of steps & kcal for a single civil day (user’s TZ). */
data class DailyMetrics(
    val date       : LocalDate,   // 2025-06-17
    val steps      : Int,         // e.g. 8 540
    val kcal       : Double                 // e.g. 350 kcal
)
