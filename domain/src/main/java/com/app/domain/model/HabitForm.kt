/**
 * @file    HabitForm.kt
 * @ingroup domain_entities
 * @brief   DTO mutable que acumula los datos del asistente de creación/edición de hábitos.
 */
package com.app.domain.entities

import com.app.domain.config.RepeatPreset
import com.app.domain.enums.*
import kotlinx.serialization.Serializable

/**
 * Estructura “en construcción” que coincide 1-a-1 con los campos que editan
 * los pasos **BasicInfo**, **Tracking** y **Notification** del wizard.
 *
 * > *No* se persiste en Room ni en Firestore; sólo vive en memoria / saved-state.
 */
@Serializable
data class HabitForm(
    /* ── Básico ───────────────────────────────────────────── */
    val name       : String               = "",
    val desc       : String               = "",
    val category   : HabitCategory        = HabitCategory.SALUD,
    val icon       : String               = "ic_favorite",

    /* ── Duración de la sesión ────────────────────────────── */
    val sessionQty : Int?                 = null,
    val sessionUnit: SessionUnit          = SessionUnit.INDEFINIDO,

    /* ── Repetición ───────────────────────────────────────── */
    val repeatPreset: RepeatPreset        = RepeatPreset.INDEFINIDO,
    /** Días de semana elegidos cuando `repeatPreset == PERSONALIZADO` (1=Lun … 7=Dom) */
    val weekDays   : Set<Int>             = emptySet(),

    /* ── Periodo total ───────────────────────────────────── */
    val periodQty  : Int?                 = null,
    val periodUnit : PeriodUnit           = PeriodUnit.INDEFINIDO,

    /* ── Extras ───────────────────────────────────────────── */
    val notify     : Boolean              = false,
    val challenge  : Boolean              = false,

    val notifMessage: String = "",
    val notifTimes  : Set<String> = emptySet(),
    val notifAdvanceMin: Int = 0
)
