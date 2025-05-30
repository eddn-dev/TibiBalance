package com.app.wear.data.repository

import com.app.wear.data.datasource.ICommunicationDataSource
import com.app.wear.data.datasource.ISensorDataSource
import com.app.wear.data.mapper.toPayload
import com.app.wear.domain.model.WearableDailyMetrics
import com.app.wear.domain.repository.IWearMetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WearMetricsRepositoryImpl @Inject constructor(
    private val sensorDataSource: ISensorDataSource,
    private val communicationDataSource: ICommunicationDataSource
) : IWearMetricsRepository {

    override suspend fun sendMetricsToCompanionApp(metrics: WearableDailyMetrics): Result<Unit> {
        return communicationDataSource.sendMetricsPayload(metrics.toPayload())
    }

    override fun getRealTimeHeartRate(): Flow<Int> = sensorDataSource.getHeartRateUpdates()

    override suspend fun getCurrentSteps(): Int = sensorDataSource.fetchCurrentStepCount()
}
