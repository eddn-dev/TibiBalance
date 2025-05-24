package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.config.NotifConfig

/* idem NotifConfigConverters.kt */
object NotifConfigConverters {
    @TypeConverter
    fun notifToJson(v: NotifConfig?): String? =
        JsonConverters.toJson(v, NotifConfig.serializer())

    @TypeConverter
    fun jsonToNotif(v: String?): NotifConfig? =
        JsonConverters.fromJson(v, NotifConfig.serializer())
}