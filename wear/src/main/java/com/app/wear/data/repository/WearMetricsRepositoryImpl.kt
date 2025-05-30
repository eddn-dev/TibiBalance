package com.app.wear.data.repository

import com.app.wear.data.datasource.ICommunicationDataSource
import com.app.wear.data.datasource.ISensorDataSource
import com.app.wear.data.mapper.toPayload // Necesitarás crear este mapper
import com.app.wear.domain.model.WearableDailyMetrics
import com.app.wear.domain.repository.IWearMetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WearMetricsRepositoryImpl @Inject constructor(
    private val sensorDataSource: ISensorDataSource,
    private val communicationDataSource: ICommunicationDataSource
) : IWearMetricsRepository {

    override suspend fun sendMetricsToCompanionApp(metrics: WearableDailyMetrics): Result<Unit> {
        // Mapear del modelo de dominio del wear al DTO/Payload de la capa de datos del wear
        val metricsPayload = metrics.toPayload() // Usando una función de extensión mapper
        return communicationDataSource.sendMetricsPayload(metricsPayload)
    }

    override fun getRealTimeHeartRate(): Flow<Int> {
        return sensorDataSource.getHeartRateUpdates()
    }

    override suspend fun getCurrentSteps(): Int {
        return sensorDataSource.fetchCurrentStepCount()
    }
}
