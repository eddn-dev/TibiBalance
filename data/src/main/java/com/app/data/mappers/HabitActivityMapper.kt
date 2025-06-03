package com.app.data.mappers

import com.app.data.local.entities.HabitActivityEntity
import com.app.domain.entities.HabitActivity
import kotlinx.datetime.LocalTime

/* Entity ➜ Domain */
fun HabitActivityEntity.toDomain(): HabitActivity =
    HabitActivity(
        id            = id,
        habitId       = habitId,
        activityDate  = activityDate,
        scheduledTime = scheduledTime ?: LocalTime(0, 0),
        opensAt       = opensAt,
        expiresAt     = expiresAt,
        status        = status,
        targetQty     = targetQty,
        recordedQty   = recordedQty,
        sessionUnit   = sessionUnit,
        loggedAt      = loggedAt,
        generatedAt   = generatedAt,
        meta          = meta
    )

fun List<HabitActivityEntity>.toDomain(): List<HabitActivity> = map { it.toDomain() }

/* Domain ➜ Entity */
fun HabitActivity.toEntity(): HabitActivityEntity =
    HabitActivityEntity(
        id            = id,
        habitId       = habitId,
        activityDate  = activityDate,
        scheduledTime = scheduledTime,                 // LocalTime? en Room
        opensAt       = opensAt,
        expiresAt     = expiresAt,
        status        = status,
        targetQty     = targetQty,
        recordedQty   = recordedQty,
        sessionUnit   = sessionUnit,
        loggedAt      = loggedAt,
        generatedAt   = generatedAt,
        meta          = meta
    )

fun List<HabitActivity>.toEntity(): List<HabitActivityEntity> = map { it.toEntity() }
