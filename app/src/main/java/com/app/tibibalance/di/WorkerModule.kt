package com.app.tibibalance.di

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.WorkManager
import com.app.tibibalance.sync.ActivitySyncWorker
import com.app.tibibalance.sync.HabitSyncWorker
import com.app.tibibalance.sync.NotificationScheduleWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides @Singleton
    fun initNotifSchedule(workerMgr: WorkManager): Operation =
        workerMgr.enqueueUniquePeriodicWork(
            "notif_schedule",
            ExistingPeriodicWorkPolicy.KEEP,
            NotificationScheduleWorker.periodicRequest()
        )

    @Provides @Singleton
    fun initActivitySync(workerMgr: WorkManager): Operation =
        workerMgr.enqueueUniquePeriodicWork(
            "activity_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            ActivitySyncWorker.periodicRequest()
        )

    @Provides @Singleton
    fun initHabitSync(workerMgr: WorkManager): Operation =
        workerMgr.enqueueUniquePeriodicWork(
            "habit_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            HabitSyncWorker.periodicRequest()
        )
}
