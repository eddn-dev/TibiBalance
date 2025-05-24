/**
 * @file    HabitActivityMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitActivityEntity ↔ HabitActivity (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.HabitActivityEntity
import com.app.domain.entities.HabitActivity

object HabitActivityMappers {

    /* Entity ➜ Domain */
    fun HabitActivityEntity.toDomain(): HabitActivity = HabitActivity(
        id          = id,
        habitId     = habitId,
        completedAt = completedAt
    )

    /* Domain ➜ Entity */
    fun HabitActivity.toEntity() = HabitActivityEntity(
        id          = id,
        habitId     = habitId,
        completedAt = completedAt,
        meta        = meta
    )
}
