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

import com.app.data.local.converters.IntSetConverter
import com.app.data.local.converters.StringSetConverter
import com.app.domain.config.PeriodUnit
import com.app.domain.config.RepeatPreset
import com.app.domain.config.SessionUnit
import com.app.domain.enums.HabitCategory

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
    @TypeConverters(IntSetConverter::class)
    val weekDays    : Set<Int>,                //!< 1=Lun … 7=Dom cuando `PERSONALIZADO`

    /* ── Periodo total ───────────────────────────────────── */
    val periodQty   : Int?,
    val periodUnit  : PeriodUnit,

    /* ── Notificaciones ─────────────────────────────────── */
    val notify      : Boolean,
    val notifMessage: String,
    @TypeConverters(StringSetConverter::class)
    val notifTimes  : Set<String>               //!< "HH:mm" en 24 h
)
