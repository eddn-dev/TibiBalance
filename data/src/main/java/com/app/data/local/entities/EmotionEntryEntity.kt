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
)