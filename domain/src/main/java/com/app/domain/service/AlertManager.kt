package com.app.domain.service

import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId

/**
 * Servicio para programar y cancelar alertas locales de hábitos.
 */
interface AlertManager {
    /** Programa las notificaciones definidas por [habit]. */
    fun schedule(habit: Habit)

    /** Cancela todas las notificaciones asociadas al hábito [id]. */
    fun cancel(id: HabitId)
}