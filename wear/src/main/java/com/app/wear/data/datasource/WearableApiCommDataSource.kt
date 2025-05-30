package com.app.wear.data.datasource

import com.app.wear.data.model.DailyMetricsPayload
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WearableApiCommDataSource @Inject constructor(
    private val dataClient: DataClient,
    private val json: Json
) : ICommunicationDataSource {

    companion object {
        private const val METRICS_PATH = "/tibibalance/metrics"
        private const val PAYLOAD_KEY = "payload_json"
    }

    override suspend fun sendMetricsPayload(payload: DailyMetricsPayload): Result<Unit> {
        return try {
            val data = json.encodeToString(payload)
            val request = PutDataMapRequest.create(METRICS_PATH).apply {
                dataMap.putString(PAYLOAD_KEY, data)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
