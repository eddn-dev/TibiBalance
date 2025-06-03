package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.enums.*
import com.app.domain.ids.*
import com.app.domain.config.Repeat
import com.app.domain.entities.DailyTipItem
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

    private val json = Json {
        classDiscriminator = "type"
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /* List<DailyTipItem> ⇄ String (JSON) ---------------------------------- */
    @TypeConverter
    fun listToString(value: List<DailyTipItem>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun stringToList(value: String?): List<DailyTipItem>? =
        value?.let { json.decodeFromString(it) }
}
