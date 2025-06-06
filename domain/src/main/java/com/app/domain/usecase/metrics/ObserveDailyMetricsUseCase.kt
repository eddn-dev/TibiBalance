package com.app.domain.usecase.metrics

import com.app.domain.model.DailyMetrics
import com.app.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDailyMetricsUseCase @Inject constructor(
    private val repo: MetricsRepository
) {
    operator fun invoke(): Flow<List<DailyMetrics>> = repo.streamDailyMetrics()
}
