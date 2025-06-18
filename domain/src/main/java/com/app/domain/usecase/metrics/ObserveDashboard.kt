package com.app.domain.usecase.metrics

import com.app.domain.entities.DashboardSnapshot
import com.app.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Flujo en tiempo real con los datos del dashboard
 * (pasos de hoy, kcal de hoy, última FC y su antigüedad).
 */
class ObserveDashboard @Inject constructor(
    private val repo: MetricsRepository
) {
    operator fun invoke(): Flow<DashboardSnapshot?> = repo.observeDashboard()
}
