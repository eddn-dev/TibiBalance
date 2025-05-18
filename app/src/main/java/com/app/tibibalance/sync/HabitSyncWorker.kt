/**
 * @file    HabitSyncWorker.kt
 * @ingroup sync
 * @brief   Reconciliación Room ↔ Firestore para hábitos.
 */
package com.app.tibibalance.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.app.data.repository.IoDispatcher
import com.app.domain.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class HabitSyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: HabitRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(io) {
        when (repo.syncNow().isSuccess) {
            true  -> Result.success()
            false -> Result.retry()
        }
    }

    companion object {
        private const val TAG = "HabitSync"

        fun periodicRequest() = PeriodicWorkRequestBuilder<HabitSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS
            )
            .addTag(TAG)
            .build()
    }
}
