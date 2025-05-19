/**
 * @file    TemplateMappers.kt
 * @ingroup data_mapper
 * @brief   HabitTemplateEntity ⇄ HabitTemplate.
 */
package com.app.data.mappers

import com.app.data.local.entities.HabitTemplateEntity
import com.app.domain.entities.HabitForm
import com.app.domain.entities.HabitTemplate

// :data/src/main/java/com/app/data/mappers/TemplateMappers.kt
/**
 * @file    TemplateMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión HabitTemplate ⇆ HabitTemplateEntity.
 */

fun HabitTemplateEntity.toDomain(): HabitTemplate = HabitTemplate(
    id       = id,
    name     = name,
    icon     = icon,
    category = category,
    formDraft = HabitForm(
        /* Básico */
        name       = name,
        desc       = "",            // Las plantillas no almacenan descripción larga
        icon       = icon,
        category   = category,

        /* Sesión */
        sessionQty = sessionQty,
        sessionUnit= sessionUnit,

        /* Repetición */
        repeatPreset= repeatPreset,
        weekDays    = weekDays,

        /* Periodo */
        periodQty   = periodQty,
        periodUnit  = periodUnit,

        /* Notif */
        notify       = notify,
        notifMessage = notifMessage,
        notifTimes   = notifTimes
    )
)

fun HabitTemplate.toEntity(): HabitTemplateEntity = HabitTemplateEntity(
    id       = id,
    name     = name,
    icon     = icon,
    category = category,

    sessionQty   = formDraft.sessionQty,
    sessionUnit  = formDraft.sessionUnit,

    repeatPreset = formDraft.repeatPreset,
    weekDays     = formDraft.weekDays,

    periodQty    = formDraft.periodQty,
    periodUnit   = formDraft.periodUnit,

    notify       = formDraft.notify,
    notifMessage = formDraft.notifMessage,
    notifTimes   = formDraft.notifTimes
)
