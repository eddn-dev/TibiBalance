package com.app.wear.data.mapper

import com.app.wear.data.model.HabitUpdatePayload
import com.app.wear.domain.model.WearableHabitInfo

fun WearableHabitInfo.toHabitUpdatePayload(completionTimestamp: Long): HabitUpdatePayload {
    return HabitUpdatePayload(
        habitId = this.id,
        isCompleted = this.isCompletedToday,
        timestamp = completionTimestamp
    )
}
