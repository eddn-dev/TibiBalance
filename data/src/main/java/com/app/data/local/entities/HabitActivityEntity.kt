/**
 * @file    HabitActivityEntity.kt
 * @ingroup data_local_entities
 * @brief   Tabla `activities` – instancias diarias/hora de un hábito-reto.
 *
 *  ▸ Se crea **una por horario** definido en `Habit.notifConfig.times`
 *    (o una única por día si la lista está vacía y el hábito es reto).
 *  ▸ Se actualiza conforme el usuario registra progreso.
 *  ▸ Se elimina en cascada cuando se borra el hábito padre.
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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

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
    indices = [Index("habitId")]
)
@TypeConverters(
    DateTimeConverters::class,   // LocalDate, LocalTime, Instant
    EnumConverters::class,       // ActivityStatus, SessionUnit
    IdConverters::class          // ActivityId, HabitId
)
data class HabitActivityEntity(

    /* ───── claves ───── */
    @PrimaryKey      val id      : ActivityId,
    @ColumnInfo(name = "habitId")
    val habitId : HabitId,

    /* ───── programación ───── */
    val activityDate : LocalDate,          // Día al que pertenece
    val scheduledTime: LocalTime?,         // Hora concreta o null (= libre)

    /* ───── progreso ───── */
    val status       : ActivityStatus = ActivityStatus.PENDING,
    val targetQty    : Int?           = null,
    val recordedQty  : Int?           = null,
    val sessionUnit  : SessionUnit?   = null,
    val loggedAt     : Instant?       = null,

    /* ───── auditoría ───── */
    val generatedAt  : Instant,            // Cuando se generó la instancia

    @Embedded(prefix = "m_")
    val meta         : SyncMeta            // sincronización Room↔Firestore
)
