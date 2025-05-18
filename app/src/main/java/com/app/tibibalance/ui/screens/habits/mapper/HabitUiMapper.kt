package com.app.tibibalance.ui.screens.habits.mapper

import com.app.domain.entities.Habit
import com.app.tibibalance.ui.screens.habits.HabitUi

internal fun Habit.toUi() = HabitUi(
    id       = id.value,
    name     = name,
    icon     = icon,
    category = category.name
)
