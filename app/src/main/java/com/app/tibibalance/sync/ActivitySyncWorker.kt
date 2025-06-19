/**
 * @file    ActivitySyncWorker.kt
 * @ingroup sync
 * @brief   Asegura actividades de hoy + genera las de mañana + push/pull Firestore.
 *
 * 1. EnsureActivitiesForDate(today)   ➜ rellena huecos sin tocar COMPLETED.
 * 2. GenerateDailyActivities(tomorrow)➜ crea slots futuros (IGNORE dupes).
 * 3. actRepo.syncNow()                ➜ LWW contra Firestore.
 */
package com.app.tibibalance.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.app.data.repository.IoDispatcher
import com.app.domain.repository.HabitActivityRepository
import com.app.domain.usecase.activity.EnsureActivitiesForDate
import com.app.domain.usecase.activity.RefreshActivitiesStatusForDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.util.concurrent.TimeUnit

@HiltWorker
class ActivitySyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val ensureToday     : EnsureActivitiesForDate,
    private val refreshStatus   : RefreshActivitiesStatusForDate,
    private val actRepo     : HabitActivityRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(io) {
        val tz     = TimeZone.currentSystemDefault()
        val today  = Clock.System.todayIn(tz)
        val result = runCatching {
            Log.w(TAG, "Ejecutando ActivitySyncWorker")
            ensureToday(today)                              // 🟢 huecos de hoy
            refreshStatus(today)                            // 🟢 actualiza estados)
            actRepo.syncNow().getOrThrow()                 // 🟢 push/pull
        }
        if (result.isSuccess) Result.success() else Result.retry()

    }

    companion object {
        private const val TAG = "ActivitySync"

        fun periodicRequest() =
            PeriodicWorkRequestBuilder<ActivitySyncWorker>(
                30, TimeUnit.MINUTES
            )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .addTag(TAG)
                .build()
    }
}
