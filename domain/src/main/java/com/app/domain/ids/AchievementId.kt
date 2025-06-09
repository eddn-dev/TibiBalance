package com.app.domain.ids

import kotlinx.serialization.Serializable

/**
 * Identificador único de un documento `achievements/{achievementId}`.
 */
@JvmInline
@Serializable
value class AchievementId(val raw: String) {
    init { require(raw.isNotBlank()) { "AchievementId no puede ser vacío" } }
    override fun toString(): String = raw
}
