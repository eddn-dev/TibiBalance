package com.app.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.domain.usecase.worker.DailyCompletionCheckScheduler // Interface import
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyCompletionCheckSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DailyCompletionCheckScheduler {

    companion object {
        private const val TAG = "DailyCheckScheduler"
    }

    override fun schedule() {
        val workManager = WorkManager.getInstance(context)
        Log.d(TAG, "Attempting to schedule DailyCompletionCheck.")

        // Optional: Define constraints if needed
        // val constraints = Constraints.Builder()
        //     .setRequiresBatteryNotLow(true)
        //     .build()

        val now = Calendar.getInstance()
        val elevenPMToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var initialDelayMillis: Long
        if (now.after(elevenPMToday)) {
            // If it's already past 11 PM today, schedule for 11 PM tomorrow.
            val elevenPMTomorrow = Calendar.getInstance().apply {
                timeInMillis = elevenPMToday.timeInMillis // Start from 11 PM today
                add(Calendar.DAY_OF_YEAR, 1) // Move to 11 PM tomorrow
            }
            initialDelayMillis = elevenPMTomorrow.timeInMillis - now.timeInMillis
            Log.d(TAG, "Scheduling for 11 PM tomorrow. Delay: $initialDelayMillis ms. Target: ${elevenPMTomorrow.time}")
        } else {
            // Schedule for 11 PM today.
            initialDelayMillis = elevenPMToday.timeInMillis - now.timeInMillis
            Log.d(TAG, "Scheduling for 11 PM today. Delay: $initialDelayMillis ms. Target: ${elevenPMToday.time}")
        }

        if (initialDelayMillis < 0) {
            // This could happen if the clock changes or calculation is extremely close to 11 PM.
            // Setting to 0 means it will run as soon as possible.
            Log.w(TAG, "Calculated negative initial delay ($initialDelayMillis ms). Setting to 0.")
            initialDelayMillis = 0
        }

        val dailyCheckRequest = OneTimeWorkRequestBuilder<UncompletedHabitsWorker>()
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            // .setConstraints(constraints) // Uncomment if constraints are defined
            .addTag(UncompletedHabitsWorker.UNIQUE_WORK_NAME) // Optional: add a tag for easier querying
            .build()

        // Enqueue as unique work. UncompletedHabitsWorker is responsible for rescheduling subsequent runs.
        workManager.enqueueUniqueWork(
            UncompletedHabitsWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE, // REPLACE ensures if it's scheduled again, the new one takes precedence.
            dailyCheckRequest
        )
        Log.i(TAG, "Enqueued ${UncompletedHabitsWorker.UNIQUE_WORK_NAME} with initial delay ${initialDelayMillis}ms.")
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UncompletedHabitsWorker.UNIQUE_WORK_NAME)
        Log.i(TAG, "Cancelled unique work: ${UncompletedHabitsWorker.UNIQUE_WORK_NAME}")
    }
}
