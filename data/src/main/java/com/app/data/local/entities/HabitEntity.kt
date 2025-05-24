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
@Entity(tableName = "habits")
@TypeConverters(
    DateTimeConverters::class,
    EnumConverters::class,
    IdConverters::class,
    RepeatConverters::class,
    NotifConfigConverters::class,
    ChallengeConfigConverters::class
)
data class HabitEntity(
    @PrimaryKey           val id        : String,
    val name              : String,
    val description       : String,
    val category          : HabitCategory,
    val icon              : String,
    @Embedded(prefix="s_")val session   : Session,
    val repeat            : Repeat,
    @Embedded(prefix="p_")val period    : Period,
    val notifConfig       : NotifConfig,
    val challenge         : ChallengeConfig?,
    val isBuiltIn         : Boolean = false,
    @Embedded(prefix="m_")val meta      : SyncMeta
)
