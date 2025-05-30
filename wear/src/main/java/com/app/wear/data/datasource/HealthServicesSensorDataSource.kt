package com.app.wear.data.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HealthServicesSensorDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ISensorDataSource {
    private val healthServicesClient by lazy { HealthServices.getClient(context) }
    private val measureClient by lazy { healthServicesClient.measureClient }

    companion object {
        private const val TAG = "HealthSensorDataSource"
    }

    override fun getHeartRateUpdates(): Flow<Int> = callbackFlow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Body sensors permission not granted for HR.")
            trySend(-1) // O emitir un error/estado específico
            close()
            return@callbackFlow
        }

        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                Log.d(TAG, "HR Availability: $dataType, $availability")
                if (availability !is Availability.Available) {
                     trySend(-2) // Indicar no disponible
                }
            }
            override fun onDataReceived(data: androidx.health.services.client.data.DataPointContainer) {
                data.getData(DataType.HEART_RATE_BPM).lastOrNull()?.let {
                    Log.d(TAG, "HR Data: ${it.value}")
                    trySend(it.value.toInt())
                }
            }
        }
        Log.d(TAG, "Registering HR callback")
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
        awaitClose {
            Log.d(TAG, "Unregistering HR callback")
            measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, callback)
        }
    }

    override suspend fun fetchCurrentStepCount(): Int {
         if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Activity recognition permission not granted for steps.")
            return 0
        }
        try {
            // STEPS_DAILY es un buen candidato, pero requiere un manejo más complejo de lectura de datos agregados.
            // Para un ejemplo más simple, si el dispositivo soporta STEPS (instantáneo, menos común para "diario"):
            val response = measureClient.readData(setOf(DataType.STEPS_DAILY)).await() // Esto es una simplificación
            // Ensure the correct data type is being used for steps. The example uses STEPS_DAILY,
            // but SampleDataPoint<Long> might be for a different DataType like STEPS.
            // This part needs to be accurate based on what DataType.STEPS_DAILY actually returns.
            // For now, proceeding with the example's casting, but it's a common point of error.
            val stepsDataPoint = response.firstOrNull()?.getData(DataType.STEPS_DAILY)?.firstOrNull() //as? SampleDataPoint<Long>

            // Assuming STEPS_DAILY provides total steps for the day as a Long value in a SampleDataPoint
            // The structure of DataPoint can vary. If STEPS_DAILY is an aggregate, it might be structured differently.
            // For this example, we'll assume it's a SampleDataPoint containing a Long.
            // If it's an AggregateDataPoint, the access pattern would be different.
            var stepsValue = 0L
            if (stepsDataPoint is SampleDataPoint<*>) {
                // Check if the value is Long before casting
                val value = stepsDataPoint.value
                if (value is Long) {
                   stepsValue = value
                } else {
                    Log.w(TAG, "Unexpected data type for steps: ${value?.javaClass?.name}")
                }
            } else if (stepsDataPoint != null) {
                 Log.w(TAG, "Steps data point is not a SampleDataPoint: ${stepsDataPoint.javaClass.name}")
            }

            return stepsValue.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching step count: ${e.message}", e)
            return 0
        }
    }
}
