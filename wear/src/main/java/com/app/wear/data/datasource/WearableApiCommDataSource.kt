package com.app.wear.data.datasource

import com.app.wear.domain.model.DailyMetricsPayload
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Serializa el payload y lo sube al Data Layer de forma urgente.
 */
class WearableApiCommDataSource @Inject constructor(
    private val dataClient: DataClient,
    private val json: Json
) {
    companion object {
        private const val METRICS_PATH = "/tibibalance/metrics"
        private const val PAYLOAD_KEY  = "payload_json"
    }

    suspend fun sendMetricsPayload(payload: DailyMetricsPayload): Result<Unit> = runCatching {
        val req = PutDataMapRequest.create(METRICS_PATH).apply {
            dataMap.putString(PAYLOAD_KEY, json.encodeToString(payload))
            //     ↑ o  dataMap[PAYLOAD_KEY] = json.encodeToString(payload)
        }
            .setUrgent()               // entrega inmediata :contentReference[oaicite:1]{index=1}
            .asPutDataRequest()

        dataClient.putDataItem(req).await()  // KTX Task.await para suspender :contentReference[oaicite:2]{index=2}
    }
}
