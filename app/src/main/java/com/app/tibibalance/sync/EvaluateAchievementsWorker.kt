package com.app.tibibalance.sync


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.app.domain.repository.HabitRepository
import com.google.firebase.Timestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Named

@HiltWorker
class EvaluarLogrosWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitRepo: HabitRepository,
    @Named("IoDispatcher") private val io: CoroutineDispatcher
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(io) {
        val userId = inputData.getString("userId") ?: return@withContext Result.failure()
        habitRepo.evaluateAchievements(userId)
        Result.success()
    }

    companion object {
        fun oneTime(userId: String) = OneTimeWorkRequestBuilder<EvaluarLogrosWorker>()
            .setInputData(workDataOf("userId" to userId))
            .build()
    }
}
