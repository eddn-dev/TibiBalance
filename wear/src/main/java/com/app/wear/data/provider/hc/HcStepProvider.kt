// :wear/data/provider/hc/HcStepProvider.kt
package com.app.wear.data.provider.hc

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.app.wear.domain.provider.StepProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * @brief Proveedor de pasos diarios usando Health Connect.
 *
 * Llama a aggregate COUNT_TOTAL dentro del rango [hoy 00:00, ahora].
 */
class HcStepProvider @Inject constructor(
    @ApplicationContext private val ctx: Context
) : StepProvider {

    /** Cliente único de Health Connect (se crea on-demand). */
    private val hc: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(ctx) }

    override suspend fun todaySteps(): Int {
        val zone = ZoneId.systemDefault()
        val request = AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(
                LocalDate.now(zone).atStartOfDay(zone).toInstant(),
                Instant.now()
            )
        )
        val result = hc.aggregate(request)
        return (result[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
    }
}

