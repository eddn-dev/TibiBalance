package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.DateTimeConverters
import com.app.domain.common.SyncMeta
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "daily_metrics")
@TypeConverters(DateTimeConverters::class)
data class DailyMetricsEntity(
    @PrimaryKey                     val date: LocalDate,
    val steps                       : Int,
    val avgHeart                    : Int?,
    val calories                    : Int?,
    val source                      : String,
    val importedAt                  : Instant,
    @Embedded(prefix = "meta_")     val meta: SyncMeta
)
