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
 * Últimos 30 días (hoy-29 … hoy) para gráficas mensuales.
 */
class ObserveLast30Days @Inject constructor(
    private val range: ObserveDailyMetricsRange
) {
    operator fun invoke(): Flow<List<DailyMetrics>> {
        val today  = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val start  = today.minus(DatePeriod(days = 29))
        return range(start, today)
    }
}
