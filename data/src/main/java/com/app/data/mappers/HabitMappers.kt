/**
 * @file    HabitMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitEntity ↔ Habit (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.HabitEntity
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId

fun HabitEntity.toDomain() = Habit(
    id          = HabitId(id),
    name        = name,
    description = description,
    category    = category,
    icon        = icon,
    session     = session,
    repeat      = repeat,
    period      = period,
    notifConfig = notifConfig,
    challenge   = challenge,
    isBuiltIn   = isBuiltIn,
    meta        = meta
)

fun Habit.toEntity() = HabitEntity(
    id          = id.raw,
    name        = name,
    description = description,
    category    = category,
    icon        = icon,
    session     = session,
    repeat      = repeat,
    period      = period,
    notifConfig = notifConfig,
    challenge   = challenge,
    isBuiltIn   = isBuiltIn,
    meta        = meta
)
