/**
 * @file    HabitActivityEntity.kt
 * @brief   Tabla `activities` con ventanas (opensAt / expiresAt).
 */
package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.DateTimeConverters
import com.app.data.local.converters.EnumConverters
import com.app.data.local.converters.IdConverters
import com.app.domain.common.SyncMeta
import com.app.domain.enums.ActivityStatus
import com.app.domain.enums.SessionUnit
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.datetime.*

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity        = HabitEntity::class,
            parentColumns = ["id"],
            childColumns  = ["habitId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "activityDate", "scheduledTime"], unique = true)
    ]
)
@TypeConverters(
    DateTimeConverters::class,   // LocalDate, LocalTime, Instant
    EnumConverters::class,       // ActivityStatus, SessionUnit
    IdConverters::class          // ActivityId, HabitId
)
data class HabitActivityEntity(

    /* ─── claves ─── */
    @PrimaryKey        val id      : ActivityId,
    @ColumnInfo(name = "habitId")
    val habitId : HabitId,

    /* ─── programación ─── */
    val activityDate  : LocalDate,
    val scheduledTime : LocalTime?,       // null ⇒ “cualquier hora”
    val opensAt       : Instant?,         // ventana inicial
    val expiresAt     : Instant?,         // ventana de caducidad

    /* ─── progreso ─── */
    val status        : ActivityStatus = ActivityStatus.PENDING,
    val targetQty     : Int?           = null,
    val recordedQty   : Int?           = null,
    val sessionUnit   : SessionUnit?   = null,
    val loggedAt      : Instant?       = null,

    /* ─── auditoría ─── */
    val generatedAt   : Instant,
    @Embedded(prefix = "m_") val meta: SyncMeta
)
