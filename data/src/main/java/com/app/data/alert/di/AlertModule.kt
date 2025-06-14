package com.app.data.alert.di

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.app.data.R
import com.app.data.alert.EmotionAlertManager
import com.app.data.alert.HabitAlertManager
import com.app.data.alert.HabitAlertReceiver
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
        alarm: AlarmManager
    ): AlertManager = HabitAlertManager(ctx, alarm)

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun provideChannelInitializer(
        @ApplicationContext ctx: Context
    ): ChannelInitializer = ChannelInitializer {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(NotificationManager::class.java)
            val ch  = NotificationChannel(
                HabitAlertReceiver.CHANNEL_HABITS,
                ctx.getString(R.string.channel_habits),
                NotificationManager.IMPORTANCE_HIGH
            )
            mgr.createNotificationChannel(ch)
        }
    }

    @Provides @Singleton
    fun provideEmotionAlertManager(
        @ApplicationContext ctx: Context,
        alarm: AlarmManager
    ): EmotionAlertManager = EmotionAlertManager(ctx, alarm)
}

fun interface ChannelInitializer { operator fun invoke() }
