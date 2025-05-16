/**
 * @file    HabitActivityMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitActivityEntity ↔ HabitActivity (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.HabitActivityEntity
import com.app.domain.entities.HabitActivity
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

object HabitActivityMappers {

    private val json = JsonConfig.default
    private val mapSer = MapSerializer(String.serializer(), String.serializer())

    fun HabitActivityEntity.toDomain(): HabitActivity = HabitActivity(
        id        = id,
        habitId   = habitId,
        type      = type,
        timestamp = timestamp,
        deviceId  = deviceId,
        payload   = json.decodeFromString(mapSer, payloadJson),
        meta      = meta
    )

    fun HabitActivity.toEntity(): HabitActivityEntity = HabitActivityEntity(
        id          = id,
        habitId     = habitId,
        type        = type,
        timestamp   = timestamp,
        deviceId    = deviceId,
        payloadJson = json.encodeToString(mapSer, payload),
        meta        = meta
    )
}
