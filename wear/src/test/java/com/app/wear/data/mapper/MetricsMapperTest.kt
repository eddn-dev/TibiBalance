package com.app.wear.data.mapper

import com.app.wear.domain.model.WearableDailyMetrics
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @file MetricsMapperTest.kt
 * @brief Pruebas para la conversión de métricas del dominio al payload.
 */
class MetricsMapperTest {
    @Test
    fun toPayload_mapsFields() {
        val domain = WearableDailyMetrics(
            steps = 10,
            heartRate = 70f,
            caloriesBurned = 1f,
            activeMinutes = 2,
            distanceMeters = 3f,
            timestamp = 1000L,
            userId = "u"
        )

        val dto = domain.toPayload()
        assertEquals(domain.steps, dto.steps)
        assertEquals(domain.heartRate, dto.heartRate)
        assertEquals(domain.timestamp, dto.timestamp)
    }
}
