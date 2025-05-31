package com.app.data.alert.di

import android.app.AlarmManager
import android.content.Context
// Removed NotificationChannel, NotificationManager, Build, R, HabitAlertReceiver imports
import com.app.data.alert.HabitNotificationScheduler // Changed import
import com.app.domain.repository.HabitRepository // Added import
import com.app.domain.service.AlertManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlertModule {

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext ctx: Context): AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    fun provideAlertManager(
        @ApplicationContext ctx: Context,
        alarm: AlarmManager,
        habitRepository: HabitRepository // Added HabitRepository
    ): AlertManager = HabitNotificationScheduler(ctx, alarm, habitRepository) // Changed to HabitNotificationScheduler

    // Removed provideChannelInitializer method
}

// Removed ChannelInitializer fun interface
