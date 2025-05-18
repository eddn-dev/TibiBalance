/**
 * @file    TemplateMappers.kt
 * @ingroup data_mapper
 * @brief   HabitTemplateEntity ⇄ HabitTemplate.
 */
package com.app.data.mappers

import com.app.data.local.entities.HabitTemplateEntity
import com.app.domain.entities.HabitForm
import com.app.domain.entities.HabitTemplate

object TemplateMappers {

    /** Instancia JSON común a todos los mapeadores */
    private val json = JsonConfig.default

    /* ----------- Room ➡︎ Dominio ----------- */
    fun HabitTemplateEntity.toDomain(): HabitTemplate = HabitTemplate(
        id         = id,
        name       = name,
        category   = category,
        icon       = icon,
        formDraft  = json.decodeFromString(HabitForm.serializer(), formJson)
    )

    /* ----------- Dominio ➡︎ Room ----------- */
    fun HabitTemplate.toEntity(): HabitTemplateEntity = HabitTemplateEntity(
        id       = id,
        name     = name,
        icon     = icon,
        category = category,
        formJson = json.encodeToString(HabitForm.serializer(), formDraft)
    )
}
