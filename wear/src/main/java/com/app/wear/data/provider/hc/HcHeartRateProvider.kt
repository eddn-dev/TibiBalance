/**
 * @file HcHeartRateProvider.kt
 * @brief Proveedor de BPM promedio diario vía Health Connect (Jetpack).
 *
 * Requiere:
 *   • Permiso READ para [HeartRateRecord] en tiempo de ejecución.
 *   • dependency: androidx.health.connect:connect-client:1.1.0-rc02
 *
 * @author  Edd
 * @date    2025-06-16
 */
package com.app.wear.data.provider.hc

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class HcHeartRateProvider @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    /** Cliente perezoso y thread-safe. */
    private val hc: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(ctx) }

    /**
     * @return BPM promedio del día o `null` si no hay muestras.
     */
    suspend fun todayAvgBpm(): Int? {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val end   = Instant.now()

        val request = AggregateRequest(
            metrics = setOf(HeartRateRecord.BPM_AVG),
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )

        val result = hc.aggregate(request)
        return result[HeartRateRecord.BPM_AVG]?.toInt()
    }
}
