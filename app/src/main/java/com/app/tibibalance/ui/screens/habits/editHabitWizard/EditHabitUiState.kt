package com.app.tibibalance.ui.screens.habits.editHabitWizard

import com.app.domain.model.HabitForm

data class EditHabitUiState(
    val currentStep             : Int       = 0,
    val form                    : HabitForm = HabitForm(),
    val showOnly                : Boolean   = true,
    val backToShowOnlyFrom3     : Boolean = false,
    val hasChanges              : Boolean   = false,
    val askExit                 : Boolean   = false,
    val saving                  : Boolean   = false,
    val savedOk                 : Boolean   = false,
    val errorMsg                : String?   = null
)
sealed interface EditEvent { object Dismiss : EditEvent }
