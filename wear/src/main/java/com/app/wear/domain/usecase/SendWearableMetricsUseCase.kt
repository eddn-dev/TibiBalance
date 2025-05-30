package com.app.wear.domain.usecase

import com.app.wear.domain.model.WearableDailyMetrics
import com.app.wear.domain.repository.IWearMetricsRepository
import javax.inject.Inject

// Caso de uso para enviar las métricas recolectadas por el wearable.
class SendWearableMetricsUseCase @Inject constructor(
    private val metricsRepository: IWearMetricsRepository
) {
    suspend operator fun invoke(metrics: WearableDailyMetrics): Result<Unit> {
        // Podría incluir lógica adicional aquí si fuera necesario antes de enviar,
        // como validaciones específicas del wearable o transformaciones.
        return metricsRepository.sendMetricsToCompanionApp(metrics)
    }
}
