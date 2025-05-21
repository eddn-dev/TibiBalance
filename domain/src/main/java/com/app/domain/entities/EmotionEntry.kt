package com.app.domain.entities

import com.app.domain.common.SyncMeta
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * @file    EmotionEntry.kt
 * @ingroup domain_entities
 * @brief   Registro diario del estado emocional (`emotions/{date}`).
 */
@Serializable
data class EmotionEntry(
    val date     : LocalDate,
    val emojiId  : String,
    val meta     : SyncMeta = SyncMeta()
)
