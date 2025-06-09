package com.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.data.local.converters.DateTimeConverters
import com.app.data.local.converters.EnumConverters
import com.app.data.local.converters.IdConverters
import com.app.data.local.converters.RepeatConverters
import com.app.data.local.dao.AchievementDao
import com.app.data.local.dao.DailyMetricsDao
import com.app.data.local.dao.DailyTipDao
import com.app.data.local.dao.EmotionEntryDao
import com.app.data.local.dao.HabitActivityDao
import com.app.data.local.dao.HabitDao
import com.app.data.local.dao.OnboardingStatusDao
import com.app.data.local.dao.UserDao
import com.app.data.local.entities.AchievementEntity
import com.app.data.local.entities.DailyMetricsEntity
import com.app.data.local.entities.DailyTipEntity
import com.app.data.local.entities.EmotionEntryEntity
import com.app.data.local.entities.HabitActivityEntity
import com.app.data.local.entities.HabitEntity
import com.app.data.local.entities.OnboardingStatusEntity
import com.app.data.local.entities.UserEntity
import com.app.domain.entities.Achievement

@Database(
    entities = [
        HabitEntity::class, HabitActivityEntity::class,
        UserEntity::class, EmotionEntryEntity::class,
        DailyMetricsEntity::class, OnboardingStatusEntity::class,
        DailyTipEntity::class,
        AchievementEntity::class,
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(
    DateTimeConverters::class, EnumConverters::class,
    IdConverters::class, RepeatConverters::class
)
abstract class AppDb : RoomDatabase() {
    abstract fun habitDao()         : HabitDao
    abstract fun activityDao()      : HabitActivityDao
    abstract fun userDao()          : UserDao
    abstract fun emotionDao()       : EmotionEntryDao
    abstract fun metricsDao()       : DailyMetricsDao
    abstract fun onboardingDao()    : OnboardingStatusDao
    abstract fun dailyTipDao()      : DailyTipDao
    abstract fun achievementDao()   : AchievementDao
}

