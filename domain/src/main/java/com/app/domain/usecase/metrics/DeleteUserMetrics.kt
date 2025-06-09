package com.app.domain.usecase.metrics

import com.app.domain.metrics.repository.UserMetricsRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para eliminar todas las métricas de una fecha dada.
 */
class DeleteUserMetrics @Inject constructor(
    private val repo: UserMetricsRepository
) {
    suspend operator fun invoke(date: LocalDate) {
        repo.deleteByDate(date)
    }
}
