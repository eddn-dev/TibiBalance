/**
 * @file    HabitActivityEntity.kt
 * @ingroup data_local_entities
 * @brief   Tabla `activities` (marca de hábito completado).
 */
package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.DateTimeConverters
import com.app.data.local.converters.IdConverters
import com.app.domain.common.SyncMeta
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.datetime.Instant

@Entity(
    tableName = "activities",
    foreignKeys = [ForeignKey(
        entity        = HabitEntity::class,
        parentColumns = ["id"],
        childColumns  = ["habitId"],
        onDelete      = ForeignKey.CASCADE
    )],
    indices = [Index("habitId")]
)
@TypeConverters(DateTimeConverters::class, IdConverters::class)
data class HabitActivityEntity(
    @PrimaryKey                   val id         : ActivityId,
    val habitId                    : HabitId,
    val completedAt                : Instant,
    @Embedded(prefix = "meta_") val meta       : SyncMeta
)
