/**
 * @file    GenerateDailyActivities.kt
 * @brief   Crea las instancias diarias de HabitActivity con ventanas válidas.
 */
package com.app.domain.usecase.activity

import com.app.domain.common.SyncMeta
import com.app.domain.config.matches
import com.app.domain.entities.*
import com.app.domain.enums.ActivityStatus
import com.app.domain.enums.SessionUnit
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitActivityRepository
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import javax.inject.Inject

class GenerateDailyActivities @Inject constructor(
    private val habitRepo: HabitRepository,
    private val actRepo  : HabitActivityRepository
) {

    /* ───────────── API ───────────── */

    suspend operator fun invoke(
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ) {
        val genAt = Clock.System.now()
        val habits = habitRepo.observeUserHabits()
            .first()
            .filter { it.challenge != null && it.repeat.matches(date) }

        val batch = habits.flatMap { generateForHabit(it, date, genAt) }
        // Log que muestra todas las actividades que se generan.
        actRepo.insertAll(batch)                           // IGNORE duplicates
    }

    /* ───────── helper reutilizable ───────── */

    internal fun generateForHabit(
        habit: Habit,
        date : LocalDate,
        genAt: Instant,
        cutoff: Instant? = null
    ): List<HabitActivity> {

        val tz        = TimeZone.currentSystemDefault()
        val advance   = habit.notifConfig.advanceMin.minutes
        val duration  = habit.session.qty?.toDuration(habit.session.unit) ?: Duration.ZERO
        val buffer    = 6.hours

        val slots: List<LocalTime?> =
            if (habit.notifConfig.times.isEmpty()) listOf(null) else habit.notifConfig.times

        val validSlots = slots.filter { lt ->
            if(lt == null) return@filter true

            if(cutoff == null) return@filter true

            val schedInstant = date.atTime(lt).toInstant(tz)
            schedInstant >= cutoff
        }

        return validSlots.map { lt ->

            /* ── cálculo de ventanas ── */
            val schedLt     = lt ?: LocalTime(0, 0)               // “cualquier hora” ⇒ 00:00
            val schedInst   = date.atTime(schedLt).toInstant(tz)

            val opensAt     =
                if (lt == null) date.startOf(tz)                  // todo el día
                else           schedInst - advance

            val expiresAt   =
                if (lt == null) date.atTime(LocalTime(23, 59, 59)).toInstant(tz)
                else           schedInst + duration + buffer

            HabitActivity(
                id            = buildId(habit.id, date, lt),
                habitId       = habit.id,
                activityDate  = date,
                scheduledTime = schedLt,
                status        = ActivityStatus.PENDING,
                targetQty     = habit.session.qty,
                recordedQty   = null,
                sessionUnit   = habit.session.unit,
                loggedAt      = null,
                generatedAt   = genAt,
                opensAt       = opensAt,
                expiresAt     = expiresAt,
                meta          = SyncMeta(
                    createdAt   = genAt,
                    updatedAt   = genAt,
                    pendingSync = true
                )
            )
        }
    }

    /* ───────── helpers privados ───────── */

    private fun Int.toDuration(unit: SessionUnit?): Duration = when (unit) {
        SessionUnit.HORAS -> this.hours
        SessionUnit.MINUTOS-> this.minutes
        else                                    -> Duration.ZERO
    }

    fun buildId(habitId: HabitId, date: LocalDate, time: LocalTime?): ActivityId =
        ActivityId("$habitId-${date.toEpochDays()}-${time ?: "any"}")

    private fun LocalDate.startOf(tz: TimeZone): Instant =
        atTime(LocalTime(0, 0)).toInstant(tz)
}
