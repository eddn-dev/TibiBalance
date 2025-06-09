package com.app.domain.usecase.metrics

import com.app.domain.metrics.entity.UserMetrics
import com.app.domain.metrics.repository.UserMetricsRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar una métrica individual.
 */
class UpdateUserMetrics @Inject constructor(
    private val repo: UserMetricsRepository
) {
    suspend operator fun invoke(metric: UserMetrics) {
        repo.update(metric)
    }
}
