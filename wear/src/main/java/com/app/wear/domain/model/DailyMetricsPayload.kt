/**
 * @file      DailyMetricsPayload.kt
 * @brief     DTO que viaja del reloj (Wear) al teléfono a través de la Data Layer.
 *
 * Contiene las métricas básicas del día calculadas con Health Connect:
 *  - **steps**  → total de pasos desde las 00:00 locales.
 *  - **avgBpm** → promedio de la frecuencia cardiaca diaria; puede ser *null*
 *                si no hubo lecturas.
 *  - **timestamp** → instante en que se generó el payload, en epoch millis UTC.
 *
 * @author    Edd
 * @date      2025-06-16
 */
package com.app.wear.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyMetricsPayload(
    val steps: Int,
    val avgBpm: Int?,
    val timestamp: Long = System.currentTimeMillis()
)
