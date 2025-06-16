package com.app.domain.entities

import com.app.domain.common.SyncMeta
import com.app.domain.enums.Emotion
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * @file    EmotionEntry.kt
 * @ingroup domain_entities
 * @brief   Registro diario del estado emocional (`emotions/{date}`).
 */
@Serializable
data class EmotionEntry(
    val date    : LocalDate,
    val mood    : Emotion,
    val meta    : SyncMeta = SyncMeta()
)
