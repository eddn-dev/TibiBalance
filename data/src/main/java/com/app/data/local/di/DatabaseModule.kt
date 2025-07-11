package com.app.data.local.di

import android.content.Context
import androidx.room.Room
import com.app.data.local.dao.*
import com.app.data.local.db.AppDb
import com.app.data.local.db.MIGRATION_1_2
import com.app.data.local.db.MIGRATION_2_3
import com.app.data.local.db.MIGRATION_3_4
import com.app.data.local.security.SecurePassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * @file    DatabaseModule.kt
 * @ingroup data_local_di
 * @brief   Proporciona AppDb cifrada y DAOs a través de Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /* ── AppDb (Room) ───────────────────────────────────────────────────── */
    @Provides
    @Singleton
    fun provideAppDb(
        @ApplicationContext ctx: Context,
        factory: SupportFactory
    ): AppDb =
        Room.databaseBuilder(ctx, AppDb::class.java, "AppDb.db")
            .openHelperFactory(factory)
            .build()


    @Provides
    @Singleton
    fun provideSqlCipherFactory(ppProvider: SecurePassphraseProvider): SupportFactory =
        ppProvider.provideFactory()


    /* ── DAOs ───────────────────────────────────────────────────────────── */
    @Provides fun provideHabitDao(db: AppDb): HabitDao                 = db.habitDao()
    @Provides fun provideActivityDao(db: AppDb): HabitActivityDao      = db.activityDao()
    @Provides fun provideUserDao(db: AppDb): UserDao                   = db.userDao()
    @Provides fun provideEmotionDao(db: AppDb): EmotionEntryDao        = db.emotionDao()
    @Provides fun provideMetricsDao(db: AppDb): DailyMetricsDao        = db.metricsDao()
    @Provides fun provideOnboardingDao(db: AppDb): OnboardingStatusDao = db.onboardingDao()
    @Provides fun provideDailyTipDao(db: AppDb): DailyTipDao           = db.dailyTipDao()
    @Provides fun provideAchievementDao(db: AppDb): AchievementDao     = db.achievementDao()
}
