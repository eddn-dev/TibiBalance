/**
 * @file    UpdateHabitUseCase.kt
 * @ingroup domain_usecase_habit
 */
package com.app.domain.usecase.habit

import com.app.domain.entities.Habit
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Reglas de negocio:
 * 1.  Solo se puede tocar `notifConfig` (cambiar `enabled`, hora, mensaje…).
 * 2.  Las plantillas (`isBuiltIn`) nunca son editables.
 *
 *  ◀︎ Nota ▶︎ Si el hábito está en modo reto **se permite** el cambio
 *            de notificaciones, pero ningún otro campo.
 */
/**  UpdateHabitUseCase.kt  */
class UpdateHabit @Inject constructor(
    private val repo     : HabitRepository,
    private val getHabit : GetHabitById
) {

    /** true ⇢ lo ÚNICO distinto entre ambos es notifConfig + meta                */
    private fun Habit.onlyNotifChanged(other: Habit): Boolean =
        this.copy(notifConfig = other.notifConfig, meta = other.meta) == other

    suspend operator fun invoke(updated: Habit) {
        val current = getHabit(updated.id).first()
            ?: throw IllegalArgumentException("Habit not found")

        /*  Plantillas nunca se modifican  */
        require(!current.isBuiltIn) { "Las plantillas sugeridas no son editables" }

        if (current.challenge != null) {
            /*  En modo reto SÓLO notificaciones  */
            require(current.onlyNotifChanged(updated)) {
                "Cuando el hábito está en reto sólo puedes cambiar las notificaciones"
            }
        }
        /* Fuera de reto → se permite cualquier campo                         */
        repo.update(updated)
    }
}

