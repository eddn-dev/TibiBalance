/**
 * @file    RepeatMatcher.kt
 * @ingroup domain_config
 * @brief   Comprueba si un [Repeat] ocurre en la [date].
 *
 *  Patrones soportados:
 *    • Repeat.Daily(every = N)
 *    • Repeat.Weekly(days = setOf(DayOfWeek…))
 *    • Repeat.Monthly(dayOfMonth = X)
 */
package com.app.domain.config

import kotlinx.datetime.LocalDate
import java.time.DayOfWeek

/* ──────────────────────────── API ──────────────────────────── */

fun Repeat.matches(date: LocalDate): Boolean = when (this) {

    Repeat.None      -> false

    /* “Cada N días” */
    is Repeat.Daily  -> date.toEpochDays() % every == 0

    /* Días concretos de la semana */
    is Repeat.Weekly -> date.dayOfWeekJava() in days

    /* Día fijo del mes */
    is Repeat.Monthly-> date.dayOfMonth == dayOfMonth

    /* Patrones no soportados */
    else             -> false
}

/* ───────────────────────── helpers ─────────────────────────── */

/** Convierte el enum de kotlinx → java.time sin usar `toJavaLocalDate()` */
private fun LocalDate.dayOfWeekJava(): DayOfWeek =
    DayOfWeek.entries[this.dayOfWeek.ordinal]   // mismos índices 0-6
