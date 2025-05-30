package com.app.wear.domain.usecase

import com.app.wear.domain.model.WearableDailyMetrics
import com.app.wear.domain.repository.IWearMetricsRepository
import javax.inject.Inject

class SendWearableMetricsUseCase @Inject constructor(
    private val repository: IWearMetricsRepository
) {
    suspend operator fun invoke(metrics: WearableDailyMetrics): Result<Unit> {
        return repository.sendMetricsToCompanionApp(metrics)
    }
}
