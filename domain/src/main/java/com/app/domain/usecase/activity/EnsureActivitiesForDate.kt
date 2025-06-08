package com.app.domain.usecase.activity

import com.app.domain.config.matches
import com.app.domain.entities.HabitActivity
import com.app.domain.repository.HabitActivityRepository
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class EnsureActivitiesForDate @Inject constructor(
    private val habitRepo: HabitRepository,
    private val actRepo  : HabitActivityRepository,
    private val dailyGen : GenerateDailyActivities
) {
    suspend operator fun invoke(date: LocalDate) {
        val habits = habitRepo.observeUserHabits().first()
            .filter { it.challenge != null && it.repeat.matches(date) }

        val genAt = Clock.System.now()
        val missing = mutableListOf<HabitActivity>()

        habits.forEach { habit ->
            val expected = habit.notifConfig.times.ifEmpty { listOf(null) }
            val exist    = actRepo.countByHabitAndDate(habit.id, date)

            if (exist < expected.size) {          // hay huecos
                missing += dailyGen.generateForHabit(habit, date, genAt)
            }
        }
        actRepo.insertAll(missing)                // IGNORE + índice único ⇒ segura
    }
}
