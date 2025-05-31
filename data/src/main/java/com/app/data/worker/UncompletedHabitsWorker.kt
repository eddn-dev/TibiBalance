package com.app.data.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.app.domain.entities.Habit
import com.app.domain.enums.NotifChannel
import com.app.domain.usecase.habit.GetUncompletedHabitsForDayUseCase
import com.app.tibibalance.MainActivity
// Import R from the app module. This assumes data module can access app module resources.
import com.app.tibibalance.R as AppR
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class UncompletedHabitsWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getUncompletedHabitsForDayUseCase: GetUncompletedHabitsForDayUseCase
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "DailyUncompletedHabitCheck" // Ensure this matches DailyCompletionCheckSchedulerImpl
        private const val TAG = "UncompletedHabitsWorker"
        const val HABIT_ID_INTENT_EXTRA_KEY = "HABIT_ID_EXTRA" // Consistent with HabitAlertReceiver
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting UncompletedHabitsWorker.")
        try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            Log.d(TAG, "Checking uncompleted habits for date: $today")
            val uncompletedHabits = getUncompletedHabitsForDayUseCase(today)
            Log.d(TAG, "Found ${uncompletedHabits.size} uncompleted habits for $today.")

            if (uncompletedHabits.isNotEmpty()) {
                uncompletedHabits.forEachIndexed { index, habit ->
                    Log.d(TAG, "Showing notification for uncompleted habit: ${habit.name}")
                    showNotificationForHabit(habit, index)
                }
            }

            Log.d(TAG, "Rescheduling next worker run.")
            rescheduleNextWork()
            Log.d(TAG, "UncompletedHabitsWorker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in UncompletedHabitsWorker", e)
            // Optionally, you might want to reschedule even on failure, depending on policy
            // For now, it will rely on WorkManager's retry mechanisms if configured, or fail.
            Result.failure()
        }
    }

    private fun showNotificationForHabit(habit: Habit, index: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(HABIT_ID_INTENT_EXTRA_KEY, habit.id.value) // Assuming habit.id.value is the String/Long ID
        }

        val pendingIntentRequestCode = "UNCOMPLETED_${habit.id.value}_${index}".hashCode()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            pendingIntentRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // These strings would ideally come from app resources (e.g., com.app.tibibalance.R)
        // For now, using placeholder strings if AppR is not correctly resolved or strings not yet defined.
        // This assumes AppR.string.uncompleted_habits_notification_title and AppR.string.uncompleted_habits_notification_text exist.
        val notificationTitle = appContext.getString(AppR.string.uncompleted_habits_notification_title_placeholder)
        val notificationText = appContext.getString(AppR.string.uncompleted_habits_notification_text_placeholder, habit.name)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, NotifChannel.HABITS.id) // Using HABITS channel ID
            .setSmallIcon(AppR.drawable.ic_notification_placeholder) // Using a placeholder icon from AppR
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = "UNCOMPLETED_HABIT_${habit.id.value}".hashCode() // Unique ID per habit

        try {
            NotificationManagerCompat.from(applicationContext).notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification shown for habit ${habit.name} with ID $notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException showing notification for ${habit.name}. Missing POST_NOTIFICATIONS permission?", e)
        }
    }

    private fun rescheduleNextWork() {
        val workManager = WorkManager.getInstance(appContext)

        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23) // 11 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If current time is already past 11 PM today, schedule for 11 PM next day.
        if (now.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1)
            Log.d(TAG, "Current time is past 11 PM. Scheduling for 11 PM next day: ${targetTime.time}")
        } else {
            Log.d(TAG, "Scheduling for 11 PM today: ${targetTime.time}")
        }
        // This original logic in the prompt was: add(Calendar.DAY_OF_YEAR, 1) unconditionally.
        // That would always schedule for the *next* day at 11 PM, even if run at 10 PM.
        // The goal is "daily check that runs around 11 PM".
        // If the worker runs at 10:59 PM, it should schedule the *next* one for 11 PM tomorrow.
        // If it runs at 11:01 PM (e.g. due to doze mode delay), it has completed today's check,
        // so it should schedule for 11 PM tomorrow.
        // The logic targetTime.add(Calendar.DAY_OF_YEAR, 1) after setting to 23:00 ensures it's always next day.
        // Let's revert to the prompt's original simpler logic which always schedules for "tomorrow 11 PM" relative to "now".

        val elevenPMNextDay = Calendar.getInstance().apply {
            // Start with current time
            add(Calendar.DAY_OF_YEAR, 1) // Move to tomorrow
            set(Calendar.HOUR_OF_DAY, 23) // Set time to 11 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Log.d(TAG, "Target reschedule time: ${elevenPMNextDay.time}")


        val initialDelay = elevenPMNextDay.timeInMillis - Calendar.getInstance().timeInMillis
        Log.d(TAG, "Calculated initial delay: $initialDelay ms")

        if (initialDelay <= 0) {
            Log.e(TAG, "Calculated negative or zero delay. This should not happen if logic is correct. Delay: $initialDelay")
            // Fallback or error handling might be needed. For now, WorkManager might run it immediately.
        }

        val dailyCheckRequest = OneTimeWorkRequestBuilder<UncompletedHabitsWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(TAG) // Add a tag for easier observation or cancellation if needed
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            dailyCheckRequest
        )
        Log.i(TAG, "Enqueued next UncompletedHabitsWorker for ${elevenPMNextDay.time} (in $initialDelay ms)")
    }
}
