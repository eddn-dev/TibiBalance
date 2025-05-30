package com.app.wear.domain.usecase

import com.app.wear.domain.repository.IWearMetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Caso de uso para observar el ritmo cardíaco en tiempo real.
class ObserveHeartRateUseCase @Inject constructor(
    private val metricsRepository: IWearMetricsRepository
) {
    operator fun invoke(): Flow<Int> {
        return metricsRepository.getRealTimeHeartRate()
    }
}
