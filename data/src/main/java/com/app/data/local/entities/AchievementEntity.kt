package com.app.data.local.entities

import androidx.room.*
import com.app.domain.common.SyncMeta
import kotlinx.datetime.Instant

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")           val id: String,
    val name                           : String,
    val description                    : String,
    val progress                       : Int,
    val unlocked                       : Boolean,
    val unlockDate                     : Instant?,          // ↔ DateTimeConverters
    @Embedded(prefix = "meta_")        val meta: SyncMeta   // createdAt, updatedAt, …
)
