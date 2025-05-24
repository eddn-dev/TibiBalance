package com.app.tibibalance.ui.screens.habits.addHabitWizard

import com.app.domain.ids.HabitId

sealed interface WizardEvent {
    object Dismiss : WizardEvent
}
