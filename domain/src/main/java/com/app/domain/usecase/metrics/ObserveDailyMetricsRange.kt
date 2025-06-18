package com.app.domain.usecase.metrics

import com.app.domain.entities.DailyMetrics
import com.app.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Históricos entre [start] (inclusive) y [end] (inclusive).
 */
class ObserveDailyMetricsRange @Inject constructor(
    private val repo: MetricsRepository
) {
    operator fun invoke(
        start: LocalDate,
        end  : LocalDate
    ): Flow<List<DailyMetrics>> = repo.observeDailyMetrics(start, end)
}
