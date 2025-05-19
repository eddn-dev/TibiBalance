/* data/mappers/RepeatMappers.kt */
package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.config.*
import com.app.domain.config.Repeat.*
import java.time.DayOfWeek

/**
 * Helpers de mapeo entre el árbol polimórfico [Repeat] y la capa UI/domain.
 */
@RequiresApi(Build.VERSION_CODES.O)            // porque usamos java.time.DayOfWeek
object RepeatMappers {

    /** Convierte cualquier implementación de [Repeat] al preset “simplificado” usado por la UI. */
    fun Repeat.toPreset(): RepeatPreset = when (this) {

        /* ───── Sin repetición ───── */
        Repeat.None                      -> RepeatPreset.INDEFINIDO

        /* ───── Diarios ───── */
        is Daily                         -> when (every) {
            1  -> RepeatPreset.DIARIO
            3  -> RepeatPreset.CADA_3_DIAS
            15 -> RepeatPreset.CADA_15_DIAS
            else -> RepeatPreset.PERSONALIZADO
        }

        /* ───── Semanales ───── */
        is Weekly                        ->
            // UI clásica: “Semanal” == sólo lunes (legacy); cualquier otra combinación → personalizado
            if (days == setOf(DayOfWeek.MONDAY))
                RepeatPreset.SEMANAL
            else
                RepeatPreset.PERSONALIZADO

        /* ───── Mensuales / otras variantes ───── */
        is Monthly, is MonthlyByWeek,
        is Yearly, is BusinessDays       -> RepeatPreset.PERSONALIZADO
    }

    /** Devuelve el conjunto de días de semana (1=Lun … 7=Dom) si el patrón es Weekly; de lo contrario, vacío. */
    fun Repeat.weekDaysSet(): Set<Int> = when (this) {
        is Weekly -> days.map(DayOfWeek::getValue).toSet()
        else      -> emptySet()
    }
}
