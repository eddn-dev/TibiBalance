package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.*
import com.app.domain.common.SyncMeta
import com.app.domain.enums.ActivityType
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.serialization.json.Json
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

/**
 * @file    HabitActivityEntity.kt
 * @ingroup data_local_entities
 * @brief   Tabla `activities` (evento histórico de un hábito).
 */
@Entity(
    tableName = "activities",
    indices = [Index("habitId")],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns  = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(
    DateTimeConverters::class,
    EnumConverters::class,
    IdConverters::class
)
data class HabitActivityEntity(
    @PrimaryKey                 val id: ActivityId,
    @ColumnInfo(index = true)   val habitId: HabitId,
    val type                    : ActivityType,
    val timestamp               : Instant,
    val deviceId                : String,
    val payloadJson             : String,
    @Embedded(prefix = "meta_") val meta: SyncMeta
) {
    companion object {
        private val json = Json { encodeDefaults = true }

        private val mapSer = MapSerializer(String.serializer(), String.serializer())

        fun fromDomain(a: com.app.domain.entities.HabitActivity): HabitActivityEntity =
            HabitActivityEntity(
                id          = a.id,
                habitId     = a.habitId,
                type        = a.type,
                timestamp   = a.timestamp,
                deviceId    = a.deviceId,
                /* 🔽 serializer explícito */
                payloadJson = json.encodeToString(mapSer, a.payload),
                meta        = a.meta
            )
    }

    fun toDomain(): com.app.domain.entities.HabitActivity {
        val jsonF = Json { ignoreUnknownKeys = true }
        return com.app.domain.entities.HabitActivity(
            id        = id,
            habitId   = habitId,
            type      = type,
            timestamp = timestamp,
            deviceId  = deviceId,
            /* 🔽 mismo serializer para deserializar */
            payload   = jsonF.decodeFromString(mapSer, payloadJson),
            meta      = meta
        )
    }

}
