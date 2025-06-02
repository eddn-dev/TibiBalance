package com.app.wear.data.datasource

import android.util.Log
import com.app.data.remote.model.DailyMetricsPayload
import com.app.wear.data.model.HabitUpdatePayload
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WearableApiCommDataSource @Inject constructor(
    private val dataClient: DataClient,
    private val json: Json // Inyectado desde Hilt
) : ICommunicationDataSource {

    companion object {
        private const val TAG = "WearableApiCommDS"
        // Paths para la Data Layer API (deben ser únicos y consistentes con la app móvil)
        private const val METRICS_DATA_PATH = "/tibibalance/metrics"
        private const val HABIT_UPDATE_DATA_PATH = "/tibibalance/habit_update"

        // Keys para los DataMap
        private const val PAYLOAD_KEY = "payload_json"
    }

    override suspend fun sendMetricsPayload(payload: DailyMetricsPayload): Result<Unit> {
        return try {
            val payloadJson = json.encodeToString(payload)
            val putDataMapReq = PutDataMapRequest.create(METRICS_DATA_PATH).apply {
                dataMap.putString(PAYLOAD_KEY, payloadJson)
                dataMap.putLong("timestamp", System.currentTimeMillis()) // Evita DataItems idénticos
            }
            val request = putDataMapReq.asPutDataRequest().setUrgent() // Usar urgent si es necesario

            dataClient.putDataItem(request).await()
            Log.i(TAG, "Metrics payload sent successfully to $METRICS_DATA_PATH")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending metrics payload to $METRICS_DATA_PATH", e)
            Result.failure(e)
        }
    }

    override suspend fun sendHabitUpdatePayload(payload: HabitUpdatePayload): Result<Unit> {
         return try {
            val payloadJson = json.encodeToString(payload)
            val putDataMapReq = PutDataMapRequest.create(HABIT_UPDATE_DATA_PATH).apply {
                dataMap.putString(PAYLOAD_KEY, payloadJson)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
            val request = putDataMapReq.asPutDataRequest().setUrgent()

            dataClient.putDataItem(request).await()
            Log.i(TAG, "Habit update payload sent successfully to $HABIT_UPDATE_DATA_PATH")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending habit update payload to $HABIT_UPDATE_DATA_PATH", e)
            Result.failure(e)
        }
    }
}
