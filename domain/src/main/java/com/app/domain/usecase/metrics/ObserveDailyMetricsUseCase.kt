package com.app.domain.usecase.metrics

/**
 * @file ObserveDailyMetricsUseCase.kt
 * @brief Caso de uso que expone un flujo con todas las métricas almacenadas.
 *
 * Utiliza el [MetricsRepository] para obtener de Room la lista de
 * [DailyMetrics] y entregarla como [Flow].
 */

import com.app.domain.entities.DailyMetrics
import com.app.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDailyMetricsUseCase @Inject constructor(
    private val repo: MetricsRepository
) {
    operator fun invoke(): Flow<List<DailyMetrics>> = repo.streamDailyMetrics()
}
