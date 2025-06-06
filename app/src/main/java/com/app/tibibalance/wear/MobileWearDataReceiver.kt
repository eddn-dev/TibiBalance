package com.app.tibibalance.wear

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.app.domain.repository.MetricsRepository
import com.app.domain.repository.HabitActivityRepository
import com.app.domain.model.DailyMetrics
import com.app.domain.enums.ActivityStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import android.util.Log
import com.app.data.remote.model.DailyMetricsPayload
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.app.data.remote.model.HabitUpdatePayload

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

    @Inject
    lateinit var habitActivityRepository: HabitActivityRepository

    override fun onDataChanged(dataEvents: DataEventBuffer) {

        Log.d(TAG, "onDataChanged ► se invocó con ${dataEvents.count()} evento(s)")
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue

            val path = event.dataItem.uri.path
            Log.d(TAG, "DataEvent recibido con path: $path")

            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            val payloadJson = dataMap.getString("payload_json")
            if (payloadJson == null) {
                Log.e(TAG, "DataMap sin key 'payload_json'")
                continue
            }

            when (path) {
                "/tibibalance/metrics" -> {
                    try {
                        val payload = json.decodeFromString<DailyMetricsPayload>(payloadJson)

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

                        ioScope.launch {
                            metricsRepository.upsert(domainMetrics)
                            Log.i(TAG, "Métricas guardadas en Room [date=$date]")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando o guardando DailyMetricsPayload: $e")
                    }
                }

                "/tibibalance/habit_update" -> {
                    try {
                        val payload = json.decodeFromString<HabitUpdatePayload>(payloadJson)
                        val instant = Instant.fromEpochMilliseconds(payload.timestamp)
                        val date = instant
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date

                        ioScope.launch {
                            val activities = habitActivityRepository.observeByDate(date).first()
                            val activity = activities.find { it.habitId.raw == payload.habitId }
                            if (activity != null) {
                                val updated = activity.copy(
                                    status = if (payload.isCompleted) ActivityStatus.COMPLETED else ActivityStatus.PENDING,
                                    loggedAt = if (payload.isCompleted) instant else null,
                                    meta = activity.meta.copy(
                                        updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                                        pendingSync = true
                                    )
                                )
                                habitActivityRepository.update(updated)
                                Log.i(TAG, "Actividad actualizada desde Wear [habitId=${payload.habitId}]")
                            } else {
                                Log.w(TAG, "Actividad no encontrada para habitId=${payload.habitId} en fecha $date")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando HabitUpdatePayload: $e")
                    }
                }

                else -> {
                    Log.d(TAG, "Se ignora path [$path]")
                }
            }
        }
    }
}
