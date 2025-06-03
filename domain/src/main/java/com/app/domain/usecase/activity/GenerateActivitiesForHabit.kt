package com.app.domain.usecase.activity

import com.app.domain.config.matches
import com.app.domain.entities.Habit
import com.app.domain.repository.HabitActivityRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import javax.inject.Inject

class GenerateActivitiesForHabit @Inject constructor(
    private val dailyGen: GenerateDailyActivities,
    private val actRepo : HabitActivityRepository
) {
    suspend operator fun invoke(habit: Habit, today: LocalDate) {
        if (habit.challenge == null) return

        val genAt = Clock.System.now()
        val listToday    = dailyGen.generateForHabit(habit, today, genAt)
        val listTomorrow = dailyGen.generateForHabit(
            habit, today.plus(DatePeriod(days = 1)), genAt
        )
        actRepo.insertAll(listToday + listTomorrow)
    }
}

