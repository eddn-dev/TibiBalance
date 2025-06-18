/* :data/src/main/kotlin/com/app/data/mappers/DailyMetricsMappers.kt */
package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.local.entities.DailyMetricsEntity
import com.app.data.remote.model.DailyMetricsDto
import com.app.domain.entities.DailyMetrics
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/* ---------- Domain ↔︎ Entity ---------- */
fun DailyMetricsEntity.toDomain() = DailyMetrics(date, steps, kcal)

fun DailyMetrics.toEntity()      = DailyMetricsEntity(date, steps, kcal)

/* ---------- Domain ↔︎ DTO -------------- */
@RequiresApi(Build.VERSION_CODES.O)
private val ISO = DateTimeFormatter.ISO_LOCAL_DATE

@RequiresApi(Build.VERSION_CODES.O)
fun DailyMetrics.toDto() = DailyMetricsDto(
    date  = date.toJavaLocalDate().format(ISO),
    steps = steps,
    kcal  = kcal
)

fun DailyMetricsDto.toDomain() = DailyMetrics(
    date  = LocalDate.parse(date),
    steps = steps,
    kcal  = kcal
)
