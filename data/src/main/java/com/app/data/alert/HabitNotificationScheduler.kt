package com.app.data.alert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.app.data.alert.HabitAlertReceiver // Corrected import
import com.app.domain.config.Repeat
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitRepository
import com.app.domain.service.AlertManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.temporal.ChronoUnit // For day differences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Assuming it's a singleton
class HabitNotificationScheduler @Inject constructor(
    @ApplicationContext private val ctx: Context, // Use @ApplicationContext for Hilt
    private val alarm: AlarmManager, // Provided by a Hilt module (e.g., AlertModule)
    private val habitRepository: HabitRepository // Injected
) : AlertManager {

    companion object {
        private const val TAG = "HabitNotificationSch"
        // Intent Extras
        const val EXTRA_HABIT_ID = "habitId"
        const val EXTRA_HABIT_NAME = "habitName"
        const val EXTRA_NOTIFICATION_MESSAGE = "notificationMessage"
        const val EXTRA_NOTIFICATION_TIME_NANO = "notificationTimeNano"
        const val EXTRA_NOTIFICATION_MODE_STR = "notificationModeStr"
        const val EXTRA_VIBRATE_STR = "vibrateStr"
    }

    override fun schedule(habit: Habit) {
        val notifConfig = habit.notifConfig
        if (notifConfig == null || !notifConfig.enabled) {
            Log.d(TAG, "Notification config disabled for habit ${habit.id.value}, cancelling any existing alarms.")
            cancel(habit.id) // Ensure this is safe to call (e.g. doesn't require habit object if it fetches)
            return
        }

        Log.d(TAG, "Scheduling notifications for habit ${habit.id.value} with ${notifConfig.times.size} time(s). Advance: ${notifConfig.advanceMin} min.")

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        for (time in notifConfig.times) {
            var triggerAtMillis = calculateNextTriggerTime(habit, time, now)

            if (triggerAtMillis == null) {
                Log.d(TAG, "No valid trigger time found for habit ${habit.id.value} at specific time $time.")
                continue
            }

            var finalTriggerMillis = triggerAtMillis - (notifConfig.advanceMin * 60 * 1000L)

            if (finalTriggerMillis <= System.currentTimeMillis()) {
                Log.d(TAG, "Calculated trigger time $finalTriggerMillis for $time is in the past. Attempting to find next occurrence.")
                // Start search strictly after the calculated past trigger time
                val nextDateTimeAfterSkipped = Instant.fromEpochMilliseconds(finalTriggerMillis).toLocalDateTime(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.NANOSECOND) // Use NANOSECOND for strict "after"
                val nextTriggerAttemptMillis = calculateNextTriggerTime(habit, time, nextDateTimeAfterSkipped)

                if (nextTriggerAttemptMillis == null) {
                    Log.w(TAG, "Could not find a future trigger for habit ${habit.id.value} at time $time after skipping a past one.")
                    continue
                }
                finalTriggerMillis = nextTriggerAttemptMillis - (notifConfig.advanceMin * 60 * 1000L)
                if (finalTriggerMillis <= System.currentTimeMillis()){
                     Log.w(TAG, "Next trigger attempt for habit ${habit.id.value} at $time is STILL in the past ($finalTriggerMillis). Skipping this time slot.")
                     continue
                }
                Log.d(TAG, "Rescheduled habit ${habit.id.value} for $time to $finalTriggerMillis (after initial was in past).")
            }


            val intent = Intent(ctx, HabitAlertReceiver::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habit.id.value) // Assuming HabitId.value is String or Long
                putExtra(EXTRA_HABIT_NAME, habit.name)
                putExtra(EXTRA_NOTIFICATION_MESSAGE, notifConfig.message)
                putExtra(EXTRA_NOTIFICATION_TIME_NANO, time.toNanoOfDay())
                putExtra(EXTRA_NOTIFICATION_MODE_STR, notifConfig.mode.name)
                putExtra(EXTRA_VIBRATE_STR, notifConfig.vibrate.toString())
            }

            // Unique request code for each habit AND time combination
            val requestCode = "${habit.id.value}_${time.toNanoOfDay()}".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                ctx,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalTriggerMillis, pendingIntent)
                Log.i(TAG, "Scheduled habit '${habit.name}' for time $time at $finalTriggerMillis (Request Code: $requestCode)")
            } catch (se: SecurityException) {
                Log.e(TAG, "SecurityException while scheduling exact alarm for habit ${habit.id.value}. Check SCHEDULE_EXACT_ALARM permission.", se)
                // Potentially notify user or fallback if this occurs
            }
        }
    }

    override fun cancel(id: HabitId) {
        // This needs the habit details (specifically times) to cancel specific PendingIntents.
        // Launch a coroutine to fetch the habit, then cancel its alarms.
        CoroutineScope(Dispatchers.IO).launch {
            // Assuming habitRepository.getHabitByIdOnce(id) is a suspend function
            val habit = habitRepository.getHabitByIdOnce(id)
            if (habit == null) {
                Log.w(TAG, "Habit ${id.value} not found for cancellation. Cannot determine notification times.")
                // Attempt to cancel a generic PI if there was one (not in current design)
                // Or, if times are not needed for cancellation (e.g. if only habitId was used in PI request code, which is not the case here)
                return@launch
            }

            val notifConfig = habit.notifConfig
            if (notifConfig != null && notifConfig.enabled) { // Only try to cancel if it might have been scheduled
                Log.d(TAG, "Cancelling notifications for habit ${habit.id.value} with ${notifConfig.times.size} time(s).")
                for (time in notifConfig.times) {
                    val requestCode = "${habit.id.value}_${time.toNanoOfDay()}".hashCode()
                    val intent = Intent(ctx, HabitAlertReceiver::class.java) // Action and component must match
                    // Extras are not strictly needed for cancellation if request code and action/component match,
                    // but FLAG_NO_CREATE means it won't create a new one if PI doesn't exist.

                    val pendingIntent = PendingIntent.getBroadcast(
                        ctx,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (pendingIntent != null) {
                        alarm.cancel(pendingIntent)
                        pendingIntent.cancel() // Also cancel the PendingIntent object itself
                        Log.i(TAG, "Cancelled alarm for habit ${habit.id.value} at time $time (Request Code: $requestCode)")
                    } else {
                        Log.d(TAG, "No alarm found to cancel for habit ${habit.id.value} at time $time (Request Code: $requestCode)")
                    }
                }
            } else {
                 Log.d(TAG, "Habit ${id.value} has notifications disabled or no config, no active alarms assumed for specific times.")
            }
        }
    }

    fun rescheduleAllEnabledHabitNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Rescheduling all enabled habit notifications.")
            // Assuming habitRepository.getAllHabitsOnce() is a suspend function
            val habits = habitRepository.getAllHabitsOnce()
            if (habits.isNullOrEmpty()) {
                Log.d(TAG, "No habits found to reschedule.")
                return@launch
            }

            habits.forEach { habit ->
                // Cancel existing first (important to avoid duplicates if logic changes)
                // The cancel function itself is async, but for rescheduling,
                // it's probably fine to fire-and-forget the cancel then re-schedule.
                // If strict sequential execution is needed, one would await/join the cancel's coroutine.
                cancel(habit.id) // This launches its own coroutine

                // Schedule if enabled
                if (habit.notifConfig != null && habit.notifConfig.enabled) {
                    schedule(habit) // This will then schedule based on current config
                }
            }
            Log.i(TAG, "Finished rescheduling ${habits.size} habits.")
        }
    }

    private fun calculateNextTriggerTime(habit: Habit, specificTime: LocalTime, now: LocalDateTime): Long? {
        val notifConfig = habit.notifConfig
        // Ensure notifConfig is not null and enabled before proceeding.
        // The public schedule() method already checks this, but as a private method, good to be defensive.
        if (notifConfig == null || !notifConfig.enabled) {
            Log.w(TAG, "calculateNextTriggerTime called for habit ${habit.id.value} with notifications disabled.")
            return null
        }

        val systemZone = TimeZone.currentSystemDefault()
        val habitStartDate = notifConfig.startsAt
        val habitEndDate = notifConfig.expiresAt

        // Optimization: If habit has already expired globally
        if (habitEndDate != null && now.date > habitEndDate) {
            Log.d(TAG, "Habit ${habit.id.value} expired on $habitEndDate. Current date: ${now.date}.")
            return null
        }

        // Determine the starting date for our calculation loop.
        // If habit has a specific start date that is in the future, use that. Otherwise, start from 'now.date'.
        var calculationDate = if (habitStartDate != null && habitStartDate > now.date) {
            Log.d(TAG, "Habit ${habit.id.value} starts in future ($habitStartDate). Starting calculation from there.")
            habitStartDate
        } else {
            now.date
        }

        val maxIterations = 365 * 5 // Safety break for 5 years of daily checks
        var iterations = 0

        while (iterations < maxIterations) {
            iterations++

            // Optimization: If current calculationDate has passed habit's expiration date
            if (habitEndDate != null && calculationDate > habitEndDate) {
                Log.d(TAG, "Calculation date $calculationDate for habit ${habit.id.value} exceeds habit end date $habitEndDate. Stopping search.")
                return null
            }

            // Ensure calculationDate is not before habitStartDate if one is set.
            // This handles cases where 'now.date' was used initially but is before a future habitStartDate.
            if (habitStartDate != null && calculationDate < habitStartDate) {
                calculationDate = habitStartDate
                Log.d(TAG, "Adjusted calculation date for habit ${habit.id.value} to its start date: $habitStartDate.")
                // No continue here, proceed to check this adjusted calculationDate
            }

            val triggerDateTime = LocalDateTime(calculationDate, specificTime)

            // If today is the calculationDate, but the specificTime has already passed 'now'
            if (calculationDate == now.date && triggerDateTime < now) {
                Log.d(TAG, "Time $specificTime on $calculationDate has already passed (now: $now). Checking next day.")
                calculationDate = calculationDate.plus(1, DateTimeUnit.DAY)
                continue // Move to the next day
            }

            val isValidBasedOnPattern = when (val pattern = notifConfig.pattern) {
                is Repeat.None -> {
                    // For Repeat.None, it should only trigger if calculationDate is the habitStartDate (or now.date if no habitStartDate)
                    // and it hasn't passed. This effectively means it's a one-time alarm if not past.
                    // However, standard interpretation of "None" is no repeat. If it's for a single specific date,
                    // startsAt should be that date, and it will trigger once.
                    // If startsAt is null, it implies "today" for a "None" pattern if not past.
                    // Let's assume it should trigger if calculationDate is the effective start (habitStartDate or now.date)
                    // and then never again. The loop structure itself ensures it won't pick a future date for None.
                    val effectiveStartDate = habitStartDate ?: now.date // Fallback to now.date if no specific start
                    calculationDate == effectiveStartDate
                }
                is Repeat.Daily -> {
                    val actualStartDateForModulo = habitStartDate ?: habit.meta.createdAt.toLocalDateTime(systemZone).date
                    ChronoUnit.DAYS.between(actualStartDateForModulo.toJavaLocalDate(), calculationDate.toJavaLocalDate()) % pattern.every == 0L
                }
                is Repeat.Weekly -> {
                    // kotlinx.datetime.DayOfWeek (Mon=1..Sun=7) aligns with java.time.DayOfWeek.getValue()
                    val currentDayOfWeekJava = java.time.DayOfWeek.of(calculationDate.dayOfWeek.isoDayNumber)
                    pattern.days.contains(currentDayOfWeekJava)
                }
                is Repeat.Monthly -> calculationDate.dayOfMonth == pattern.dayOfMonth
                is Repeat.Yearly -> calculationDate.monthNumber == pattern.month && calculationDate.dayOfMonth == pattern.day
                is Repeat.MonthlyByWeek -> {
                    Log.w(TAG, "Repeat.MonthlyByWeek pattern for habit ${habit.id.value} is not fully implemented. Skipping.")
                    false // Placeholder
                }
                is Repeat.BusinessDays -> {
                    val day = calculationDate.dayOfWeek
                    val isBusinessDay = day != kotlinx.datetime.DayOfWeek.SATURDAY && day != kotlinx.datetime.DayOfWeek.SUNDAY
                    if (!isBusinessDay) {
                        false
                    } else {
                        if (pattern.every == 1) {
                            true
                        } else {
                            Log.w(TAG, "Repeat.BusinessDays pattern with 'every > 1' for habit ${habit.id.value} is not fully implemented. Skipping.")
                            false // Placeholder for 'every > 1'
                        }
                    }
                }
                // Should be RepeatPattern instead of Repeat if that's the sealed interface name
                // else -> false // Should not happen with a sealed interface covering all cases
            }

            if (isValidBasedOnPattern) {
                Log.d(TAG, "Found valid trigger date for habit ${habit.id.value}: $triggerDateTime based on pattern ${notifConfig.pattern}.")
                return triggerDateTime.toInstant(systemZone).toEpochMilliseconds()
            }

            // If not valid for today, advance to the next day and continue the loop.
            calculationDate = calculationDate.plus(1, DateTimeUnit.DAY)
        }

        Log.w(TAG, "Exceeded max iterations ($maxIterations) for habit ${habit.id.value} and time $specificTime. No valid schedule found.")
        return null // Loop finished without finding a valid date
    }
}
