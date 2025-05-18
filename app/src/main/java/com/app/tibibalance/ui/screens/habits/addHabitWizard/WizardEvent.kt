/**
 * Eventos one-shot que el ViewModel emite hacia la UI.
 */
package com.app.tibibalance.ui.screens.habits.addHabitWizard

sealed interface WizardEvent {

    /** El hábito se guardó correctamente. */
    object Saved : WizardEvent

    /** Ocurrió un error al guardar o sincronizar. */
    data class Error(val message: String) : WizardEvent
}
