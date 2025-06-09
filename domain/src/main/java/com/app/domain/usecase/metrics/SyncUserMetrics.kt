package com.app.domain.usecase.metrics

import com.app.domain.metrics.entity.UserMetrics
import com.app.domain.metrics.repository.UserMetricsRepository
import javax.inject.Inject

/**
 * Caso de uso para sincronizar un lote de métricas (local + remoto).
 */
class SyncUserMetrics @Inject constructor(
    private val repo: UserMetricsRepository
) {
    suspend operator fun invoke(metrics: List<UserMetrics>) {
        repo.sync(metrics)
    }
}
