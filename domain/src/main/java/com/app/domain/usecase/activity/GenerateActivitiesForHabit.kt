package com.app.domain.usecase.activity

import com.app.domain.entities.Habit
import com.app.domain.repository.HabitActivityRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GenerateActivitiesForHabit @Inject constructor(
    private val dailyGen: GenerateDailyActivities,
    private val actRepo : HabitActivityRepository
) {
    suspend operator fun invoke(habit: Habit, today: LocalDate) {
        if (habit.challenge == null) return

        val now = Clock.System.now()
        val listToday    = dailyGen.generateForHabit(habit, today, now, cutoff = now)

        actRepo.insertAll(listToday)
    }
}

