/**
 * @file    HabitActivityMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitActivityEntity ⇆ HabitActivity (dominio).
 *
 *  - Incluye extensiones individuales y de lista.
 *  - Refleja todos los campos de la nueva entidad (fecha, hora, estado, etc.).
 */
package com.app.data.mappers

import com.app.data.local.entities.HabitActivityEntity
import com.app.domain.entities.HabitActivity
import kotlinx.datetime.LocalTime

/* ──────────────────────────  Entity ➜ Domain  ────────────────────────── */

fun HabitActivityEntity.toDomain(): HabitActivity =
    HabitActivity(
        id           = id,
        habitId      = habitId,
        activityDate = activityDate,
        scheduledTime= scheduledTime?: LocalTime(0, 0),
        status       = status,
        targetQty    = targetQty,
        recordedQty  = recordedQty,
        sessionUnit  = sessionUnit,
        loggedAt     = loggedAt,
        generatedAt  = generatedAt,
        meta         = meta
    )

fun List<HabitActivityEntity>.toDomain(): List<HabitActivity> =
    map { it.toDomain() }

/* ──────────────────────────  Domain ➜ Entity  ────────────────────────── */

fun HabitActivity.toEntity(): HabitActivityEntity =
    HabitActivityEntity(
        id           = id,
        habitId      = habitId,
        activityDate = activityDate,
        scheduledTime= scheduledTime,
        status       = status,
        targetQty    = targetQty,
        recordedQty  = recordedQty,
        sessionUnit  = sessionUnit,
        loggedAt     = loggedAt,
        generatedAt  = generatedAt,
        meta         = meta
    )

fun List<HabitActivity>.toEntity(): List<HabitActivityEntity> =
    map { it.toEntity() }
