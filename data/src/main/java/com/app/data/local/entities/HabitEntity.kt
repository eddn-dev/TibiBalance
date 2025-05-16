package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.*
import com.app.domain.common.SyncMeta
import com.app.domain.config.*
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.HabitId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @file    HabitEntity.kt
 * @ingroup data_local_entities
 * @brief   Representa la tabla `habits` en la base Room.
 */
@Entity(
    tableName = "habits"
)
@TypeConverters(
    DateTimeConverters::class,
    EnumConverters::class,
    IdConverters::class,
    RepeatConverters::class

)
data class HabitEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")           val id: HabitId,
    val name                           : String,
    val description                    : String,
    val category                       : HabitCategory,
    val icon                           : String,
    @Embedded(prefix = "session_")     val session    : Session,
    @ColumnInfo(name = "repeat_json")  val repeat     : Repeat,
    @Embedded(prefix = "period_")      val period     : Period,
    @ColumnInfo(name = "notif_json")   val notifConfigJson: String,
    @ColumnInfo(name = "challenge_json") val challengeJson: String?,
    @Embedded(prefix = "meta_")        val meta: SyncMeta
)