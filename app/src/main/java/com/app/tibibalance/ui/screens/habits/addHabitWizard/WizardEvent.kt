package com.app.tibibalance.ui.screens.habits.addHabitWizard

import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked

sealed interface WizardEvent {
    object Dismiss : WizardEvent
    data class ShowAchievement(val logro: AchievementUnlocked) : WizardEvent
}
