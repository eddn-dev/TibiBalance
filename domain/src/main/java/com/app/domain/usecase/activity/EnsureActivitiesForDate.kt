package com.app.domain.usecase.activity

import com.app.domain.config.matches
import com.app.domain.entities.HabitActivity
import com.app.domain.repository.HabitActivityRepository
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class EnsureActivitiesForDate @Inject constructor(
    private val habitRepo: HabitRepository,
    private val actRepo  : HabitActivityRepository,
    private val dailyGen : GenerateDailyActivities
) {
    suspend operator fun invoke(date: LocalDate) {
        val tz      = TimeZone.currentSystemDefault()
        val now     = Clock.System.now()
        val habits = habitRepo.observeUserHabits().first()
            .filter { it.challenge != null && it.repeat.matches(date) }

        val genAt = Clock.System.now()
        val missing = mutableListOf<HabitActivity>()

        habits.forEach { habit ->
            /* ❶ Corte solo si la fecha es la de creación */
            val createdOn = habit.meta.createdAt          // o habit.createdAt si existe
                .toLocalDateTime(tz).date
            val cutoff   = if (date == createdOn) habit.meta.createdAt else null

            /* ❷ Genera la lista “válida” para ese día */
            val shouldExist = dailyGen.generateForHabit(habit, date, now, cutoff)

            /* ❸ ¿Cuántas ya existen? */
            val exist = actRepo.countByHabitAndDate(habit.id, date)

            if (exist < shouldExist.size) {        // ¡huecos con la misma regla!
                missing += shouldExist
            }
        }
        actRepo.insertAll(missing)                 // IGNORE dupes
    }
}
