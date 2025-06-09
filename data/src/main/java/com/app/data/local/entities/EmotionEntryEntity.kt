package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.DateTimeConverters
import com.app.data.local.converters.EnumConverters
import com.app.domain.common.SyncMeta
import com.app.domain.enums.Emotion           // 🆕
import kotlinx.datetime.LocalDate

@Entity(tableName = "emotions")
@TypeConverters(DateTimeConverters::class, EnumConverters::class)
data class EmotionEntryEntity(
    @PrimaryKey            val date: LocalDate,
    val mood               : Emotion,        // ← era String
    @Embedded(prefix = "m_")
    val meta               : SyncMeta
)
