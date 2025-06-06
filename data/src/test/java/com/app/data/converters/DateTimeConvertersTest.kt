package com.app.data.converters

import com.app.data.local.converters.DateTimeConverters
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @file DateTimeConvertersTest.kt
 * @brief Pruebas unitarias para los convertidores de fecha y hora.
 */
class DateTimeConvertersTest {

    @Test
    fun instantRoundTrip() {
        val now = Instant.fromEpochMilliseconds(1234567890)
        val long = DateTimeConverters.instantToLong(now)
        val restored = DateTimeConverters.longToInstant(long)
        assertEquals(now, restored)
    }

    @Test
    fun localDateRoundTrip() {
        val date = LocalDate.parse("2024-01-01")
        val str = DateTimeConverters.localDateToString(date)
        val back = DateTimeConverters.stringToLocalDate(str)
        assertEquals(date, back)
    }

    @Test
    fun localTimeRoundTrip() {
        val time = LocalTime.parse("08:30")
        val str = DateTimeConverters.localTimeToString(time)
        val back = DateTimeConverters.stringToLocalTime(str)
        assertEquals(time, back)
    }
}
