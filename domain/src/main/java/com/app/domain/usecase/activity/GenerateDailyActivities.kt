/**
 * @file    GenerateDailyActivities.kt
 */
package com.app.domain.usecase.activity

import com.app.domain.common.SyncMeta
import com.app.domain.config.matches
import com.app.domain.entities.*
import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.repository.HabitActivityRepository
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject

class GenerateDailyActivities @Inject constructor(
    private val habitRepo: HabitRepository,
    private val actRepo  : HabitActivityRepository
) {

    /** Genera todas las actividades del [date] indicado. */
    suspend operator fun invoke(
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ) {
        val genAt  = Clock.System.now()
        val habits = habitRepo.observeUserHabits()
            .first()
            .filter { it.challenge != null && it.repeat.matches(date) }

        val batch = habits.flatMap { generateForHabit(it, date, genAt) }

        actRepo.insertAll(batch)                     // IGNORE dupes
    }

    /* ───────────────────────── helper reutilizable ───────────────────────── */

    internal fun generateForHabit(
        habit: Habit,
        date : LocalDate,
        genAt: Instant
    ): List<HabitActivity> {

        val slots: List<LocalTime?> =
            if (habit.notifConfig.times.isEmpty()) listOf(null)
            else habit.notifConfig.times

        return slots.map { lt ->
            HabitActivity(
                id            = ActivityId(UUID.randomUUID().toString()),
                habitId       = habit.id,
                activityDate  = date,
                scheduledTime = lt?: LocalTime(0, 0),                  // null ⇒ “cualquier hora”
                status        = ActivityStatus.PENDING,
                targetQty     = habit.session.qty,
                recordedQty   = null,
                sessionUnit   = habit.session.unit,
                loggedAt      = null,
                generatedAt   = genAt,
                meta          = SyncMeta(
                    createdAt   = genAt,
                    updatedAt   = genAt,
                    pendingSync = true
                )
            )
        }
    }
}
