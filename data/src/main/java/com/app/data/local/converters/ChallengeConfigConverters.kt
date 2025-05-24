package com.app.data.local.converters

import androidx.room.TypeConverter
import com.app.domain.config.ChallengeConfig

object ChallengeConfigConverters {
    @TypeConverter
    fun challToJson(v: ChallengeConfig?): String? =
        JsonConverters.toJson(v, ChallengeConfig.serializer())

    @TypeConverter
    fun jsonToChall(v: String?): ChallengeConfig? =
        JsonConverters.fromJson(v, ChallengeConfig.serializer())
}