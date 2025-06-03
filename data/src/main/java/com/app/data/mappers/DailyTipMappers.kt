/* :data/mappers/DailyTipMappers.kt */
package com.app.data.mappers

import com.app.data.local.entities.DailyTipEntity
import com.app.domain.entities.DailyTip
import kotlinx.datetime.LocalDate

fun DailyTip.toEntity(lastShown: LocalDate? = null) = DailyTipEntity(
    id, title, subtitle, icon, content, active, lastShown
)

fun DailyTipEntity.toDomain() = DailyTip(
    id, title, subtitle, icon, content, active
)
