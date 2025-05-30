package com.app.wear.data.datasource

import com.app.wear.data.model.DailyMetricsPayload

interface ICommunicationDataSource {
    suspend fun sendMetricsPayload(payload: DailyMetricsPayload): Result<Unit>
}
