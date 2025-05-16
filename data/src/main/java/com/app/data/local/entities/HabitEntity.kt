package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.*
import com.app.domain.common.SyncMeta
import com.app.domain.config.*
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.HabitId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @file    HabitEntity.kt
 * @ingroup data_local_entities
 * @brief   Representa la tabla `habits` en la base Room.
 */
@Entity(
    tableName = "habits"
)
@TypeConverters(
    DateTimeConverters::class,
    EnumConverters::class,
    IdConverters::class,
    RepeatConverters::class

)
data class HabitEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")           val id: HabitId,
    val name                           : String,
    val description                    : String,
    val category                       : HabitCategory,
    val icon                           : String,
    @Embedded(prefix = "session_")     val session    : Session,
    @ColumnInfo(name = "repeat_json")  val repeat     : Repeat,
    @Embedded(prefix = "period_")      val period     : Period,
    @ColumnInfo(name = "notif_json")   val notifConfigJson: String,
    @ColumnInfo(name = "challenge_json") val challengeJson: String?,
    @Embedded(prefix = "meta_")        val meta: SyncMeta
) {
    companion object {
        private val json = Json { encodeDefaults = true; classDiscriminator = "_type" }

        /** Conversión domain → entity */
        fun fromDomain(h: com.app.domain.entities.Habit): HabitEntity =
            HabitEntity(
                id              = h.id,
                name            = h.name,
                description     = h.description,
                category        = h.category,
                icon            = h.icon,
                session         = h.session,
                repeat          = h.repeat,
                period          = h.period,
                /* 🔽 serializer explícito */
                notifConfigJson = json.encodeToString(
                    com.app.domain.config.NotifConfig.serializer(),
                    h.notifConfig
                ),
                challengeJson   = h.challenge?.let {
                    json.encodeToString(
                        ChallengeConfig.serializer(),
                        it
                    )
                },
                meta            = h.meta
            )
    }

    /** Conversión entity → domain */
    fun toDomain(): com.app.domain.entities.Habit {
        val jsonF = Json { ignoreUnknownKeys = true; classDiscriminator = "_type" }

        return com.app.domain.entities.Habit(
            id          = id,
            name        = name,
            description = description,
            category    = category,
            icon        = icon,
            session     = session,
            repeat      = repeat,
            period      = period,
            notifConfig = jsonF.decodeFromString(
                NotifConfig.serializer(),
                notifConfigJson
            ),
            challenge   = challengeJson?.let {
                jsonF.decodeFromString(
                    ChallengeConfig.serializer(),
                    it
                )
            },
            meta        = meta
        )
    }
}
