package com.app.wear.domain.usecase

import com.app.wear.domain.repository.IWearMetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHeartRateUseCase @Inject constructor(
    private val repository: IWearMetricsRepository
) {
    operator fun invoke(): Flow<Int> = repository.getRealTimeHeartRate()
}
