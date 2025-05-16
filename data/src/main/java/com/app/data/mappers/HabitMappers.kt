/**
 * @file    HabitMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitEntity ↔ Habit (dominio).
 */
package com.app.data.mapper

import com.app.data.local.entities.HabitEntity
import com.app.data.mappers.JsonConfig
import com.app.domain.entities.Habit
import com.app.domain.config.ChallengeConfig
import com.app.domain.config.NotifConfig

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
}
