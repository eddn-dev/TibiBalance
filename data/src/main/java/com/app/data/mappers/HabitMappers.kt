/**
 * @file    HabitMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitEntity ↔ Habit (dominio).
 */
package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.local.entities.HabitEntity
import com.app.data.remote.model.HabitDto
import com.app.domain.common.SyncMeta
import com.app.domain.entities.Habit
import com.app.domain.config.ChallengeConfig
import com.app.domain.config.NotifConfig
import com.app.domain.config.Period
import com.app.domain.config.Repeat
import com.app.domain.config.Session
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.HabitId
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

object HabitMappers {

    private val json = JsonConfig.default

    /** Entity → Domain */
    fun HabitEntity.toDomain(): Habit = Habit(
        id          = id,
        name        = name,
        description = description,
        category    = category,
        icon        = icon,
        session     = session,
        repeat      = repeat,
        period      = period,
        notifConfig = json.decodeFromString(NotifConfig.serializer(), notifConfigJson),
        challenge   = challengeJson?.let {
            json.decodeFromString(ChallengeConfig.serializer(), it)
        },
        meta        = meta
    )

    /** Domain → Entity */
    fun Habit.toEntity(): HabitEntity = HabitEntity(
        id              = id,
        name            = name,
        description     = description,
        category        = category,
        icon            = icon,
        session         = session,
        repeat          = repeat,
        period          = period,
        notifConfigJson = json.encodeToString(NotifConfig.serializer(), notifConfig),
        challengeJson   = challenge?.let {
            json.encodeToString(ChallengeConfig.serializer(), it)
        },
        meta            = meta
    )

    /* ---------------- Entity → Dto ---------------- */
    fun HabitEntity.toDto(): HabitDto = HabitDto(
        id          = id.value,
        name        = name,
        description = description,
        category    = category.name,
        icon        = icon,

        session     = json.encodeToString(Session.serializer(), session),
        repeat      = json.encodeToString(Repeat.serializer(), repeat),
        period      = json.encodeToString(Period.serializer(), period),
        notifConfig = notifConfigJson,
        challenge   = challengeJson,

        createdAt   = meta.createdAt .toTimestamp(),
        updatedAt   = meta.updatedAt .toTimestamp(),
        deletedAt   = meta.deletedAt?.toTimestamp()
    )

    /* ---------------- Dto → Entity ---------------- */
    @RequiresApi(Build.VERSION_CODES.O)
    fun HabitDto.toEntity(): HabitEntity = HabitEntity(
        id              = HabitId(id),
        name            = name,
        description     = description,
        category        = HabitCategory.valueOf(category),
        icon            = icon,

        session         = json.decodeFromString(Session.serializer(), session),
        repeat          = json.decodeFromString(Repeat.serializer(), repeat),
        period          = json.decodeFromString(Period.serializer(), period),
        notifConfigJson = notifConfig,
        challengeJson   = challenge,

        meta = SyncMeta(
            createdAt    = createdAt?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
            updatedAt    = updatedAt?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
            deletedAt    = deletedAt?.toInstant()?.toKotlinInstant(),
            pendingSync  = false                 // remoto nunca está pendiente
        )
    )
}
