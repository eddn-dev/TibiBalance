package com.app.domain.usecase.metrics

import com.app.domain.repository.MetricsRepository
import javax.inject.Inject

/**
 * Fuerza la agregación de “ayer” y su upsert + push remoto.
 * Retorna Result<Unit> para poder propagar éxito / error a la UI o Worker.
 */
class SyncDailyMetrics @Inject constructor(
    private val repo: MetricsRepository
) {
    suspend operator fun invoke() : Result<Unit> = repo.syncDailyMetrics()
}
