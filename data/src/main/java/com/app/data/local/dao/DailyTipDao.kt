package com.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.data.local.entities.DailyTipEntity
import kotlinx.datetime.LocalDate

/* :data/local/dao/DailyTipDao.kt */
@Dao
interface DailyTipDao {

    /* ①  Tip ya fijado para hoy */
    @Query("SELECT * FROM daily_tips WHERE lastShown = :today LIMIT 1")
    suspend fun currentTip(today: LocalDate): DailyTipEntity?

    /* ②  Elegibles (no vistos hoy) */
    @Query("""
        SELECT * FROM daily_tips 
         WHERE active = 1 
           AND (lastShown IS NULL OR lastShown < :today)
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    suspend fun pickRandomEligible(today: LocalDate): DailyTipEntity?

    @Upsert
    suspend fun upsertAll(list: List<DailyTipEntity>)

    @Query("UPDATE daily_tips SET lastShown = :today WHERE id = :id")
    suspend fun markShown(id: Int, today: LocalDate)
}
