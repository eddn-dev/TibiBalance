package com.app.tibibalance.ui.common.validation

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.config.RepeatPreset
import com.app.domain.enums.*
import com.app.domain.model.HabitForm
import java.time.LocalTime

/** Reglas de negocio centralizadas para ambos wizards (alta y edición). */
object HabitFormValidator {

    /** Devuelve *true* si el `step` indicado es válido para el formulario `f`. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isStepValid(step: Int, f: HabitForm): Boolean = when (step) {
        /* ── Paso 1: nombre obligatorio ─────────────────────────────── */
        1 -> f.name.isNotBlank()

        /* ── Paso 2: reglas de seguimiento ──────────────────────────── */
        2 -> when {
            /* reto exige periodo definido */
            f.challenge &&
                    (f.periodUnit == PeriodUnit.INDEFINIDO || f.periodQty == null)           -> false

            /* repetición personalizada exige días de semana */
            f.repeatPreset == RepeatPreset.PERSONALIZADO && f.weekDays.isEmpty()         -> false

            /* periodQty obligatorio si se eligió unidad */
            f.periodUnit != PeriodUnit.INDEFINIDO && f.periodQty == null                -> false

            /* sessionQty obligatorio si se eligió unidad */
            f.sessionUnit != SessionUnit.INDEFINIDO && f.sessionQty == null             -> false

            else -> true
        }

        /* ── Paso 3: notificaciones ────────────────────────────────── */
        3 -> validateNotification(f)

        /* Pasos 0 ó  4+: siempre válidos */
        else -> true
    }

    /* ---------------------------------------------------------------- */
    /*            VALIDACIÓN DETALLADA DE NOTIFICACIONES                */
    /* ---------------------------------------------------------------- */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateNotification(f: HabitForm): Boolean {
        /* Si el usuario no quiere notificar, siempre es válido */
        if (!f.notify) return true

        /* 1. Al menos una hora de recordatorio */
        if (f.notifTimes.isEmpty()) return false

        /* 2. Límite de repeticiones (0-5) y de intervalo (>0) */
        if (f.notifRepeatQty !in 0..5) return false
        if (f.notifRepeatQty > 0 && f.notifSnoozeMin <= 0) return false

        /* 3. Sin solaparse con la siguiente hora ni pasar de 23:59 */
        val minsPerDay   = 24 * 60
        val baseTimesMin = f.notifTimes
            .map { LocalTime.parse(it) }
            .sorted()
            .map { it.hour * 60 + it.minute }

        val span = f.notifRepeatQty * f.notifSnoozeMin                    // minutos extra

        baseTimesMin.forEachIndexed { idx, baseMin ->
            val lastRem  = baseMin + span                                 // última repetición
            val nextBase = baseTimesMin.getOrNull(idx + 1) ?: minsPerDay  // 24 h = tope

            /* 3a. No debe alcanzar el día siguiente */
            if (lastRem >= minsPerDay) return false

            /* 3b. No debe chocar con la siguiente hora */
            if (lastRem >= nextBase) return false
        }

        return true
    }
}
