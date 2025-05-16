package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.enums.*
import com.app.domain.ids.*
import com.app.domain.config.Repeat
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/* ── Date/Time ───────────────────────────────────────────────────────────── */
object DateTimeConverters {

    /* Instant ⇄ Long ------------------------------------------------------ */
    @TypeConverter
    fun instantToLong(value: Instant?): Long? =
        value?.toEpochMilliseconds()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? =
        value?.let { Instant.fromEpochMilliseconds(it) }
    //            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  llamada directa
    // si prefieres referencia:
    // value?.let(Instant.Companion::fromEpochMilliseconds)

    /* LocalDate ⇄ String --------------------------------------------------- */
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? =
        value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? =
        value?.let(LocalDate::parse)

    /* LocalTime ⇄ String --------------------------------------------------- */
    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? =
        value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? =
        value?.let(LocalTime::parse)
}
