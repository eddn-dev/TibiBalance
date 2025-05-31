package com.app.domain.usecase.worker

import javax.inject.Inject

/**
 * Interface for a scheduler that manages a daily check,
 * for instance, for uncompleted habits.
 * Implemented in the data or app layer (e.g., using WorkManager).
 */
interface DailyCompletionCheckScheduler {
    /**
     * Schedules the daily check.
     * If already scheduled, implementations might replace or keep existing.
     */
    fun schedule()

    /**
     * Cancels any scheduled daily check.
     */
    fun cancel()
}

/**
 * Use case to trigger the scheduling of the daily completion check.
 */
class ScheduleDailyCompletionCheckUseCase @Inject constructor(
    private val scheduler: DailyCompletionCheckScheduler
) {
    operator fun invoke() {
        scheduler.schedule()
    }
}

/**
 * Use case to cancel the daily completion check.
 */
class CancelDailyCompletionCheckUseCase @Inject constructor(
    private val scheduler: DailyCompletionCheckScheduler
) {
    operator fun invoke() {
        scheduler.cancel()
    }
}
