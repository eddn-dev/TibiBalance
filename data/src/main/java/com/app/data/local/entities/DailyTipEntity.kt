package com.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.domain.entities.DailyTipItem
import kotlinx.datetime.LocalDate

@Entity(tableName = "daily_tips")
data class DailyTipEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subtitle: String,
    val icon: String,
    val content: List<DailyTipItem>,   // ← ya concreto
    val active: Boolean,
    val lastShown: LocalDate?          // null = nunca mostrado
)

