package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.config.Repeat
import kotlinx.serialization.encodeToString        // ⬅️ extensiones
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object RepeatConverters {

    private val json = Json {
        encodeDefaults      = true
        classDiscriminator  = "_type"
        ignoreUnknownKeys   = true
    }

    /* Repeat ⇄ String ----------------------------------------------------- */
    @TypeConverter
    fun repeatToJson(value: Repeat?): String? =
        value?.let { json.encodeToString(Repeat.serializer(), it) }
    //                               ^^^^^^^^^^^^^^^^^^^^^
    // 1º arg:  serializer explícito
    // 2º arg:  objeto a serializar (`it`)

    @TypeConverter
    fun jsonToRepeat(value: String?): Repeat? =
        value?.let { json.decodeFromString(Repeat.serializer(), it) }
}
