package com.app.domain.usecase.habit

import android.util.Log // Added for logging placeholders
import com.app.domain.config.Repeat
import com.app.domain.entities.Habit
import com.app.domain.repository.HabitRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
// import kotlinx.datetime.toKotlinLocalDate // Not strictly needed if working with kotlinx.LocalDate primarily
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetUncompletedHabitsForDayUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(date: LocalDate): List<Habit> {
        val allHabits = habitRepository.getHabitsOnce()
        val activitiesForDay = habitRepository.getHabitActivitiesByDate(date)

        return allHabits.filter { habit ->
            val nc = habit.notifConfig
            // 1. Habit has notifications enabled and is within its general start/expiry range for the given date
            // Note: nc could be null if a habit was created without notification settings.
            // The problem description for HabitNotificationScheduler schedule() checks for nc == null.
            // Here, if nc is null, nc.enabled would crash. So, check for nc != null first.
            if (nc == null || !nc.enabled) return@filter false

            val isActiveDateRange = (nc.startsAt == null || !date.isBefore(nc.startsAt)) &&
                                    (nc.expiresAt == null || !date.isAfter(nc.expiresAt))

            if (!isActiveDateRange) return@filter false

            // 2. Habit is active today based on its specific pattern
            val anchorDateForPattern = nc.startsAt ?: habit.meta.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val isActiveBasedOnPattern = checkPatternForDate(nc.pattern, date, anchorDateForPattern)

            if (!isActiveBasedOnPattern) return@filter false

            // 3. Habit is not completed today
            val isCompleted = activitiesForDay.any { activity ->
                activity.habitId == habit.id &&
                activity.completedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date == date
            }

            !isCompleted
        }
    }

    // Helper function to check if a habit with a given pattern is active on a specific 'checkDate'
    // 'patternAnchorDate' is nc.startsAt or habit.meta.createdAt.date (fallback), used as the starting point.
    private fun checkPatternForDate(
        pattern: Repeat,
        checkDate: LocalDate,
        patternAnchorDate: LocalDate
    ): Boolean {
        // This check ensures that we don't consider dates before the pattern officially starts.
        // For example, a daily habit starting tomorrow should not be active today.
        if (checkDate.isBefore(patternAnchorDate) && pattern !is Repeat.Weekly && pattern !is Repeat.None) {
             // For Repeat.None, checkDate == patternAnchorDate is the specific check.
             // For Repeat.Weekly, the day of week is absolute, but we still shouldn't list it if checkDate is before startsAt.
             // This pre-check simplifies individual when branches if checkDate < patternAnchorDate implies inactive.
             // However, specific patterns might need finer control.
             // Re-evaluating: This top-level check might be too broad.
             // Let's handle it per pattern where it makes sense.
        }


        return when (pattern) {
            is Repeat.None -> {
                // Active only on the specific 'patternAnchorDate' (effective nc.startsAt or creation date).
                checkDate == patternAnchorDate
            }
            is Repeat.Daily -> {
                if (checkDate.isBefore(patternAnchorDate)) return false
                ChronoUnit.DAYS.between(patternAnchorDate.toJavaLocalDate(), checkDate.toJavaLocalDate()) % pattern.every == 0L
            }
            is Repeat.Weekly -> {
                // Weekly check is absolute for the day of the week.
                // However, it should only be considered active on or after the patternAnchorDate.
                if (checkDate.isBefore(patternAnchorDate)) return false

                val checkDayOfWeek = checkDate.dayOfWeek // kotlinx.datetime.DayOfWeek
                // pattern.days is Set<java.time.DayOfWeek>
                // java.time.DayOfWeek.of() takes an int (1 for Monday, 7 for Sunday)
                pattern.days.contains(java.time.DayOfWeek.of(checkDayOfWeek.isoDayNumber))
                // This assumes "every week". If "every X weeks" was a feature, it'd need patternAnchorDate for modulo.
            }
            is Repeat.Monthly -> {
                if (checkDate.isBefore(patternAnchorDate)) return false
                // Active if the day of the month matches, and it's on or after the anchor month/year.
                // The checkDate.isBefore(patternAnchorDate) already ensures we don't go to previous months/years.
                checkDate.dayOfMonth == pattern.dayOfMonth
            }
            is Repeat.Yearly -> {
                if (checkDate.isBefore(patternAnchorDate)) return false
                // Active if month and day match, on or after anchor year.
                // checkDate.isBefore(patternAnchorDate) ensures we don't go to previous years.
                 checkDate.monthNumber == pattern.month && checkDate.dayOfMonth == pattern.day
            }
            is Repeat.MonthlyByWeek -> {
                Log.w("GetUncompletedHabits", "Repeat.MonthlyByWeek not fully implemented in checkPatternForDate for habit.")
                false
            }
            is Repeat.BusinessDays -> {
                if (checkDate.isBefore(patternAnchorDate)) return false
                val day = checkDate.dayOfWeek
                val isBusinessDay = day != kotlinx.datetime.DayOfWeek.SATURDAY && day != kotlinx.datetime.DayOfWeek.SUNDAY
                if (!isBusinessDay) return false
                if (pattern.every == 1) return true

                Log.w("GetUncompletedHabits", "Repeat.BusinessDays with every > 1 not fully implemented in checkPatternForDate for habit.")
                false
            }
            // exhaustive when block for sealed interface might not need else in some Kotlin versions if all subtypes are covered.
            // Adding else for safety or if Repeat is not sealed/has more types not covered by imports.
            // else -> false
        }
    }

    // Helper extension to check if a LocalDate is before another LocalDate
    private fun LocalDate.isBefore(other: LocalDate): Boolean {
        return this.toEpochDays() < other.toEpochDays()
    }

    // Helper extension to check if a LocalDate is after another LocalDate
    private fun LocalDate.isAfter(other: LocalDate): Boolean {
        return this.toEpochDays() > other.toEpochDays()
    }
}
