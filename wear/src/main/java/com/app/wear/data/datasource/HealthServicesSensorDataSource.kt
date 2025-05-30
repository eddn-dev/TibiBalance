package com.app.wear.data.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.unregisterMeasureCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

class HealthServicesSensorDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ISensorDataSource {

    private val measureClient by lazy { HealthServices.getClient(context).measureClient }

    /* ──────────── FC en tiempo real ──────────── */
    override fun getHeartRateUpdates(): Flow<Int> = callbackFlow {
        if (!hasPerm(Manifest.permission.BODY_SENSORS)) {
            trySend(-1); close(); return@callbackFlow
        }

        val callback = object : MeasureCallback {

            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Sólo consideramos disponible si el estado es EXACTAMENTE AVAILABLE
                if (availability is DataTypeAvailability &&
                    availability != DataTypeAvailability.AVAILABLE
                ) {
                    trySend(-2)   // sensor sin datos o ausente
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                data.getData(DataType.HEART_RATE_BPM)
                    .lastOrNull()
                    ?.value
                    ?.roundToInt()
                    ?.let { trySend(it) }
            }
        }

        // Registro (Unit en 1.0.0)
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            launch {
                measureClient.unregisterMeasureCallback(
                    DataType.HEART_RATE_BPM,
                    callback
                )
            }
        }
    }

    /* ──────────── Pasos diarios ──────────── */
    override suspend fun fetchCurrentStepCount(): Int = 0

    /* ───────── util ───────── */
    private fun hasPerm(p: String) =
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
}
