// :data/src/main/java/com/app/data/local/entities/HabitTemplateEntity.kt
/**
 * @file    HabitTemplateEntity.kt
 * @ingroup data_local_entities
 * @brief   Representación persistente de una plantilla en la base Room.
 *
 * @details
 *  - Cada campo se mapea a una columna real: métricas, repetición, notificación, etc.
 *  - Permite indexar, filtrar y migrar sin deserializar un blob JSON.
 *  - Converters para `Set<Int>` y `Set<String>` (horas HH:mm) declarados más abajo.
 */
package com.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.Index
import com.app.data.local.converters.IntSetConverter
import com.app.data.local.converters.StringSetConverter
import com.app.domain.config.RepeatPreset
import com.app.domain.enums.HabitCategory
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit

@Entity(
    tableName = "habit_templates",
    indices   = [Index("category"), Index("notify")]
)
data class HabitTemplateEntity(
    /* ── Identidad ───────────────────────────────────────── */
    @PrimaryKey val id   : String,
    val name     : String,
    val icon     : String,
    val category : HabitCategory,

    /* ── Sesión ──────────────────────────────────────────── */
    val sessionQty  : Int?,
    val sessionUnit : SessionUnit,

    /* ── Repetición ──────────────────────────────────────── */
    val repeatPreset: RepeatPreset,
    @field:TypeConverters(IntSetConverter::class)
    val weekDays    : Set<Int>,                //!< 1=Lun … 7=Dom cuando `PERSONALIZADO`

    /* ── Periodo total ───────────────────────────────────── */
    val periodQty   : Int?,
    val periodUnit  : PeriodUnit,

    /* ── Notificaciones ─────────────────────────────────── */
    val notify      : Boolean,
    val notifMessage: String,
    @field:TypeConverters(StringSetConverter::class)
    val notifTimes  : Set<String>               //!< "HH:mm" en 24 h
)
