package com.app.wear.domain.usecase

import com.app.wear.domain.repository.IWearMetricsRepository
import javax.inject.Inject

class GetCurrentStepsUseCase @Inject constructor(
    private val metricsRepository: IWearMetricsRepository
) {
    suspend operator fun invoke(): Int {
        return metricsRepository.getCurrentSteps()
    }
}
