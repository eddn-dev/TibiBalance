package com.app.data.remote.model

import com.app.domain.entities.HabitActivity
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/* data/remote/model/HabitActivityDto.kt */
@Serializable
data class HabitActivityDto(
    val id          : String,
    val habitId     : String,
    val completedAt : Instant
) {
    fun toDomain() = HabitActivity(
        id          = ActivityId(id),
        habitId     = HabitId(habitId),
        completedAt = completedAt
    )
    companion object {
        fun fromDomain(a: HabitActivity) = HabitActivityDto(
            id          = a.id.raw,
            habitId     = a.habitId.raw,
            completedAt = a.completedAt
        )
    }
}
