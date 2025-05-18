package com.app.tibibalance.di

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.app.tibibalance.sync.HabitSyncWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    fun provideWorkManager(context: Application) = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideHabitSyncInit(workManager: WorkManager): androidx.work.Operation =
        workManager.enqueueUniquePeriodicWork(
            "habit_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            HabitSyncWorker.periodicRequest()
        )

}
