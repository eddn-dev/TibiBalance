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

// :data/mappers/TemplateMappers.kt
fun HabitTemplateEntity.toDomain() = HabitTemplate(
    id       = id,
    name     = name,
    icon     = icon,
    category = category,
    formDraft = HabitForm(
        /* Básico */
        name = name,
        desc = desc,                   //  👈  antes era ""
        icon = icon,
        category = category,

        /* Sesión */
        sessionQty  = sessionQty,
        sessionUnit = sessionUnit,

        /* Repetición */
        repeatPreset = repeatPreset,
        weekDays     = weekDays,

        /* Periodo */
        periodQty  = periodQty,
        periodUnit = periodUnit,

        /* Notif */
        notify          = notify,
        notifMessage    = notifMessage,
        notifTimes      = notifTimes,
        notifAdvanceMin = advanceMin   //  👈  NUEVO
    )
)

fun HabitTemplate.toEntity() = HabitTemplateEntity(
    id       = id,
    name     = name,
    icon     = icon,
    category = category,

    desc        = formDraft.desc,            //  👈
    sessionQty  = formDraft.sessionQty,
    sessionUnit = formDraft.sessionUnit,

    repeatPreset = formDraft.repeatPreset,
    weekDays     = formDraft.weekDays,

    periodQty  = formDraft.periodQty,
    periodUnit = formDraft.periodUnit,

    notify       = formDraft.notify,
    notifMessage = formDraft.notifMessage,
    notifTimes   = formDraft.notifTimes,
    advanceMin   = formDraft.notifAdvanceMin    //  👈
)
