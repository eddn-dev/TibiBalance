/* data/local/converters/JsonConverters.kt */
package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.data.mappers.JsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonConverters {

    private val json: Json = JsonConfig.default   // tu singleton

    /** Genérico: de objeto a String JSON */
    @TypeConverter
    fun <T> toJson(value: T?, serializer: KSerializer<T>): String? =
        value?.let { json.encodeToString(serializer, it) }

    /** Genérico: de String JSON a objeto */
    @TypeConverter
    fun <T> fromJson(value: String?, serializer: KSerializer<T>): T? =
        value?.let { json.decodeFromString(serializer, it) }
}
