/**
 * @file    EmotionMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión EmotionEntryEntity ↔ EmotionEntry (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.EmotionEntryEntity
import com.app.domain.entities.EmotionEntry

object EmotionMappers {

    fun EmotionEntryEntity.toDomain(): EmotionEntry = EmotionEntry(
        date  = date,
        emojiId = emojiId,
        meta  = meta
    )

    fun EmotionEntry.toEntity(): EmotionEntryEntity = EmotionEntryEntity(
        date    = date,
        emojiId = emojiId,
        meta    = meta
    )
}
