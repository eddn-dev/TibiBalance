package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.DateTimeConverters
import com.app.domain.common.SyncMeta
import kotlinx.datetime.LocalDate

@Entity(tableName = "emotions")
@TypeConverters(DateTimeConverters::class)
data class EmotionEntryEntity(
    @PrimaryKey                    val date: LocalDate,
    val emojiId                    : String,
    @Embedded(prefix = "meta_")    val meta: SyncMeta
) {
    companion object {
        fun fromDomain(e: com.app.domain.entities.EmotionEntry) = EmotionEntryEntity(
            date    = e.date,
            emojiId = e.emojiId,
            meta    = e.meta
        )
    }
    fun toDomain() = com.app.domain.entities.EmotionEntry(
        date     = date,
        emojiId  = emojiId,
        meta     = meta
    )
}
