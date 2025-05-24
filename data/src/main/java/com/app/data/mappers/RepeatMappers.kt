/* data/mappers/RepeatMappers.kt */
package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.config.*
import com.app.domain.enums.OccurrenceInMonth
import java.time.DayOfWeek

/**
 * Helpers de mapeo entre el árbol polimórfico [Repeat] y la capa UI/domain.
 */

/* ---------- presets ⇄ Repeat ------------------------------------------ */

@RequiresApi(Build.VERSION_CODES.O)
fun RepeatPreset.toRepeat(weekDays: Set<Int> = emptySet()): Repeat = when (this) {
    RepeatPreset.INDEFINIDO   -> Repeat.None
    RepeatPreset.DIARIO       -> Repeat.Daily()
    RepeatPreset.CADA_3_DIAS  -> Repeat.Daily(3)
    RepeatPreset.CADA_SEMANA,
    RepeatPreset.SEMANAL      -> Repeat.Weekly(setOf(DayOfWeek.MONDAY))     // default
    RepeatPreset.CADA_15_DIAS,
    RepeatPreset.QUINCENAL    -> Repeat.Daily(15)
    RepeatPreset.MENSUAL      -> Repeat.Monthly(1)
    RepeatPreset.ULTIMO_VIERNES_MES -> Repeat.MonthlyByWeek(
        dayOfWeek   = DayOfWeek.FRIDAY,
        occurrence  = OccurrenceInMonth.LAST
    )
    RepeatPreset.DIAS_LABORALES -> Repeat.BusinessDays()
    RepeatPreset.PERSONALIZADO   -> {
        val dow = weekDays.map { DayOfWeek.of(it) }.toSet()
        if (dow.isEmpty()) Repeat.None else Repeat.Weekly(dow)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Repeat.toPreset(): RepeatPreset = when (this) {
    Repeat.None                       -> RepeatPreset.INDEFINIDO
    is Repeat.Daily   -> when (every) {
        1    -> RepeatPreset.DIARIO
        3    -> RepeatPreset.CADA_3_DIAS
        15   -> RepeatPreset.CADA_15_DIAS
        else -> RepeatPreset.PERSONALIZADO      // genérico
    }
    is Repeat.Weekly -> if (days == setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    ) RepeatPreset.DIAS_LABORALES
    else RepeatPreset.PERSONALIZADO
    is Repeat.Monthly              -> RepeatPreset.MENSUAL
    is Repeat.MonthlyByWeek        -> RepeatPreset.ULTIMO_VIERNES_MES
    is Repeat.Yearly, is Repeat.BusinessDays -> RepeatPreset.PERSONALIZADO
}
