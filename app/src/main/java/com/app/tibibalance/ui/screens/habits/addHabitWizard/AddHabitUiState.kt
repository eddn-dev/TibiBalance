package com.app.tibibalance.ui.screens.habits.addHabitWizard

import com.app.domain.entities.Habit
import com.app.domain.entities.HabitForm

/**
 * UI-state global del wizard.
 *
 * currentStep  → 0-3
 * form         → datos introducidos
 * saving       → true mientras se llama a CreateHabitUseCase
 * errorMsg     → null ↔ error
 */
data class AddHabitUiState(
    val currentStep : Int = 0,
    val form        : HabitForm = HabitForm(),
    val saving      : Boolean = false,
    val errorMsg    : String?  = null,
    val hasChanges  : Boolean = false,
    val askExit     : Boolean = false,
    val askReplace  : Boolean = false,
    val savedOk     : Boolean = false,
    val tempTpl     : Habit? = null
)
