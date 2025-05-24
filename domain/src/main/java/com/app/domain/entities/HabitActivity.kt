// com/app/domain/entities/HabitActivity.kt
package com.app.domain.entities

import com.app.domain.common.SyncMeta
import com.app.domain.enums.ActivityType
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HabitActivity(
    val id        : ActivityId,
    val habitId   : HabitId,
    val completedAt : Instant           = Instant.DISTANT_PAST,
    val meta      : SyncMeta           = SyncMeta()
)
