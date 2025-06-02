package com.app.tibibalance.wear

import android.util.Log
import com.app.data.local.db.AppDb
import com.app.data.repository.DailyMetricsRepository
import com.app.data.remote.model.DailyMetricsPayload
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MobileWearDataReceiver : WearableListenerService() {
    private val TAG = "MobileWearReceiver"

    init {
        // Este init se ejecuta cuando Android instancia el servicio
        Log.d(TAG, "MobileWearDataReceiver ► creado")
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    // Lazy-load del repositorio
    private val repository: DailyMetricsRepository by lazy {
        val db = AppDb.getInstance(applicationContext)
        DailyMetricsRepository(db.metricsDao())
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged ► se invocó con ${dataEvents.count()} evento(s)")
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue

            val path = event.dataItem.uri.path
            Log.d(TAG, "DataEvent recibido con path: $path")

            // Solo procesamos /tibibalance/metrics
            if (path != "/tibibalance/metrics") {
                Log.d(TAG, "Se ignora path [$path]")
                continue
            }

            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val payloadJson = dataMap.getString("payload_json")
            if (payloadJson == null) {
                Log.e(TAG, "DataMap sin key 'payload_json'")
                continue
            }


            try {
                val payload = json.decodeFromString<DailyMetricsPayload>(payloadJson)
                ioScope.launch {
                    repository.saveFromPayload(payload)
                    Log.i(TAG, "Métricas guardadas en Room [timestamp=${payload.timestamp}]")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializando DailyMetricsPayload: $e")
            }
        }
    }
}
