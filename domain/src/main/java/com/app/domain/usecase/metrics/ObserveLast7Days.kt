package com.app.domain.usecase.metrics

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import com.app.domain.entities.DailyMetrics

/**
 * Conveniencia para gráficas/secciones que necesitan última semana
 * (hoy y los 6 días anteriores => 7 puntos).
 */
class ObserveLast7Days @Inject constructor(
    private val range: ObserveDailyMetricsRange
) {
    operator fun invoke(): Flow<List<DailyMetrics>> {
        val today  = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val start  = today.minus(DatePeriod(days = 6))
        return range(start, today)
    }
}
