package com.app.tibibalance.wear

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.app.domain.repository.MetricsRepository
import com.app.domain.model.DailyMetrics
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import android.util.Log
import com.app.data.remote.model.DailyMetricsPayload
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class MobileWearDataReceiver : WearableListenerService() {
    private val TAG = "MobileWearReceiver"

    init {
        // Este init se ejecuta cuando Android instancia el servicio
        Log.d(TAG, "MobileWearDataReceiver ► creado")
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    @Inject
    lateinit var metricsRepository: MetricsRepository

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
                // 1) Convertimos el JSON al DTO (DailyMetricsPayload):
                val payload = json.decodeFromString<DailyMetricsPayload>(payloadJson)

                // 2) Mapeamos el DTO a nuestro modelo de dominio (DailyMetrics):
                val instant = Instant.fromEpochMilliseconds(payload.timestamp)
                val date = instant
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

                val domainMetrics = DailyMetrics(
                    date = date,
                    steps = payload.steps,
                    avgHeart = payload.heartRate?.toInt(),
                    calories = payload.caloriesBurned?.toInt(),
                    source = "wear_os",
                    importedAt = instant,
                    pendingSync = true
                )

                // 3) Guardamos la métrica en Room a través de MetricsRepository
                ioScope.launch {
                    metricsRepository.upsert(domainMetrics)
                    Log.i(TAG, "Métricas guardadas en Room [date=$date]")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializando o guardando DailyMetricsPayload: $e")
            }

        }
    }
}
