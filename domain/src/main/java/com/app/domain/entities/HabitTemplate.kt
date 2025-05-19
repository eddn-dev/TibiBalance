// :domain/src/main/java/com/app/domain/entities/HabitTemplate.kt
package com.app.domain.entities

import com.app.domain.enums.HabitCategory
import kotlinx.serialization.Serializable

// :domain/src/main/java/com/app/domain/entities/HabitTemplate.kt
/**
 * @file    HabitTemplate.kt
 * @ingroup domain_entities
 * @brief   Plantilla pre-definida que el usuario puede importar al wizard.
 */
@Serializable
data class HabitTemplate(
    val id        : String,           //!< ID del documento en `habitTemplates/{id}`
    val name      : String,           //!< Nombre visible en el catálogo
    val icon      : String,           //!< Icono Material
    val category  : HabitCategory,    //!< Clasificación principal
    val formDraft : HabitForm         //!< Valores precargados para el asistente
)
