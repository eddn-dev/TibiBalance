/**
 * @file    HabitFormMappers.kt
 * @ingroup data_mapper
 * @brief   Funciones de ayuda para convertir HabitForm ⇆ Habit.
 */
package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.config.*
import com.app.domain.config.Repeat.*
import com.app.domain.entities.*
import com.app.domain.enums.*
import com.app.domain.ids.HabitId
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import java.time.DayOfWeek
import java.util.*

object HabitFormMappers {

    /* ──────────────────── public helpers ─────────────────── */

    /** Rellena un formulario nuevo con la plantilla seleccionada. */
    fun HabitForm.prefillFromTemplate(tpl: HabitTemplate): HabitForm =
        tpl.formDraft.copy()              // sólo hacemos una copia defensiva

    /**
     * Convierte el formulario (más la config de notificación final) en
     * una entidad [Habit] lista para persistir.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun HabitForm.toHabit(
        notif: NotifConfig,
        id: HabitId = HabitId(UUID.randomUUID().toString())
    ): Habit = Habit(
        id          = id,
        name        = name.trim(),
        description = desc.trim(),
        category    = category,
        icon        = icon,
        session     = Session(sessionQty, sessionUnit),
        repeat      = repeatPreset.toRepeat(weekDays),
        period      = Period(periodQty, periodUnit),
        notifConfig = notif,
        challenge   = if (challenge) defaultChallenge() else null
    )

    /* ──────────────────── private helpers ────────────────── */

    /** Traduce un [RepeatPreset] al árbol polimórfico `Repeat`.  */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun RepeatPreset.toRepeat(days: Set<Int>): Repeat = when (this) {
        RepeatPreset.INDEFINIDO -> Repeat.None
        RepeatPreset.DIARIO     -> Daily()
        RepeatPreset.CADA_3_DIAS-> Daily(3)
        RepeatPreset.SEMANAL,   // alias legacy
        RepeatPreset.CADA_SEMANA-> Weekly(setOf(DayOfWeek.MONDAY))
        RepeatPreset.CADA_15_DIAS-> Daily(15)
        RepeatPreset.MENSUAL    -> Monthly(1)
        RepeatPreset.ULTIMO_VIERNES_MES ->
            MonthlyByWeek(DayOfWeek.FRIDAY, OccurrenceInMonth.LAST)
        RepeatPreset.DIAS_LABORALES -> BusinessDays()
        RepeatPreset.QUINCENAL  -> Monthly(2)
        RepeatPreset.PERSONALIZADO -> {
            if (days.isEmpty()) Repeat.None
            else Weekly(
                days.map { it.toDayOfWeek() }
                    .toSet()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Int.toDayOfWeek(): DayOfWeek = DayOfWeek.of(coerceIn(1,7))

    private fun defaultChallenge(): ChallengeConfig {
        val start = Clock.System.now()
        return ChallengeConfig(start = start, end = start)   // se rellenará luego en servicio
    }


    fun HabitForm.toNotifConfig(): NotifConfig {
        val parsedTimes = notifTimes.mapNotNull { runCatching { LocalTime.parse(it) }.getOrNull() }
        val pattern     = when (repeatPreset) {
            RepeatPreset.DIARIO          -> Repeat.Daily()
            RepeatPreset.CADA_3_DIAS     -> Repeat.Daily(3)
            RepeatPreset.CADA_15_DIAS    -> Repeat.Daily(15)
            else                         -> Repeat.None
        }

        return NotifConfig(
            enabled    = notify,
            message    = notifMessage.ifBlank { "¡Es hora!" },
            times      = parsedTimes,
            pattern    = pattern,
            advanceMin = notifAdvanceMin,
            mode       = NotifMode.SOUND,
            vibrate    = true
        )
    }
}
