package com.app.tibibalance.di

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.WorkManager
import com.app.tibibalance.sync.ActivitySyncWorker
import com.app.tibibalance.sync.EmotionScheduleWorker
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
    fun provideWorkManager(app: Application): WorkManager =
        WorkManager.getInstance(app)

    /* ---------- inicia sincronía de hábitos ---------- */
    @Provides
    @Singleton
    fun provideHabitSyncInit(wm: WorkManager): Operation =
        wm.enqueueUniquePeriodicWork(
            "habit_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            HabitSyncWorker.periodicRequest()
        )

    /* ---------- inicia sincronía de actividades ---------- */
    @Provides
    @Singleton
    fun provideActivitySyncInit(wm: WorkManager): Operation =
        wm.enqueueUniquePeriodicWork(
            "activity_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            ActivitySyncWorker.periodicRequest()
        )

    @Provides @Singleton
    fun provideEmotionScheduleInit(wm: WorkManager): Operation =
        EmotionScheduleWorker.enqueue(wm)

}

