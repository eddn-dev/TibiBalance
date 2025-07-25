// com/app/domain/ids/ActivityId.kt
package com.app.domain.ids

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ActivityId(val raw: String) {
    val value: String
        get() = raw

    init { require(raw.isNotBlank()) { "ActivityId no puede ser vacío" } }
    override fun toString(): String = raw
}
