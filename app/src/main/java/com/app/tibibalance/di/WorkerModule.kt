package com.app.tibibalance.di

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.WorkManager
import com.app.tibibalance.sync.ActivitySyncWorker
import com.app.tibibalance.sync.AppWorker
import com.app.tibibalance.sync.HabitSyncWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides @Singleton
    fun initAppWorker(workerMgr: WorkManager): Operation =
        workerMgr.enqueueUniquePeriodicWork(
            "notif_schedule",
            ExistingPeriodicWorkPolicy.KEEP,
            AppWorker.periodicRequest()
        )
}
