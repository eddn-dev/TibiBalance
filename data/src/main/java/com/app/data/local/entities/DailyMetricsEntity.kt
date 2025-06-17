/* :data/src/main/kotlin/com/app/data/local/entities/DailyMetricsEntity.kt */
package com.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.app.data.local.converters.DateTimeConverters

@Entity(tableName = "daily_metrics")
@TypeConverters(DateTimeConverters::class)
data class DailyMetricsEntity(
    @PrimaryKey val date: kotlinx.datetime.LocalDate,
    val steps: Int,
    val kcal : Double
)
