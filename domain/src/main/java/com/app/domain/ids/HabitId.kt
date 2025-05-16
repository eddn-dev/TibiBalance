// com/app/domain/ids/HabitId.kt
package com.app.domain.ids

import kotlinx.serialization.Serializable

/**
 * @file    HabitId.kt
 * @ingroup domain_ids
 * @brief   Identificador único de un documento `habits/{habitId}`.
 */
@JvmInline
@Serializable
value class HabitId(val raw: String) {

    /** Alias para compatibilidad con repos / mappers anteriores. */
    val value: String
        get() = raw

    init { require(raw.isNotBlank()) { "HabitId no puede ser vacío" } }

    override fun toString(): String = raw
}
