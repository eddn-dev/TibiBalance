/**
 * @file    HabitForm.kt
 * @ingroup domain_entities
 * @brief   DTO mutable que acumula los datos del asistente de creación/edición de hábitos.
 */
package com.app.domain.model

import com.app.domain.config.RepeatPreset
import com.app.domain.enums.*
import kotlinx.serialization.Serializable

/**
 * Estructura “en construcción” que coincide 1-a-1 con los campos que editan
 * los pasos **BasicInfo**, **Tracking** y **Notification** del wizard.
 *
 * > *No* se persiste en Room ni en Firestore; sólo vive en memoria / saved-state.
 */
/* domain/model/HabitForm.kt */

@Serializable
data class HabitForm(

    /* ── Básico ───────────────────────────── */
    val name         : String        = "",
    val desc         : String        = "",
    val category     : HabitCategory = HabitCategory.SALUD,
    val icon         : String        = "ic_favorite",

    /* ── Duración sesión ──────────────────── */
    val sessionQty   : Int?          = null,
    val sessionUnit  : SessionUnit   = SessionUnit.INDEFINIDO,

    /* ── Repetición ───────────────────────── */
    val repeatPreset : RepeatPreset  = RepeatPreset.INDEFINIDO,
    val weekDays     : Set<Int>      = emptySet(),          // 1-7 (L-D)

    /* ── Periodo ──────────────────────────── */
    val periodQty    : Int?          = null,
    val periodUnit   : PeriodUnit    = PeriodUnit.INDEFINIDO,

    /* ── Extras ───────────────────────────── */
    val notify           : Boolean    = false,
    val challenge        : Boolean    = false,

    /* ---- Notificación ---- */
    val notifMessage     : String     = "",
    val notifTimes       : Set<String> = emptySet(),         // “HH:mm”
    val notifAdvanceMin  : Int        = 0,
    val notifMode        : NotifMode  = NotifMode.SILENT,
    val notifVibrate     : Boolean    = false,
    val notifRepeatQty   : Int        = 0,
    val notifSnoozeMin    : Int = 10,
    val notifStartsAt    : String?    = null                // “yyyy-MM-dd”
)
