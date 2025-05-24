/* data/local/converters/RepeatConverters.kt */
package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.config.Repeat
import kotlinx.serialization.serializer

object RepeatConverters {
    @TypeConverter
    fun repeatToJson(v: Repeat?): String? =
        JsonConverters.toJson(v, Repeat.serializer())

    @TypeConverter
    fun jsonToRepeat(v: String?): Repeat? =
        JsonConverters.fromJson(v, Repeat.serializer())
}