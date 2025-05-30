package com.app.wear.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HabitUpdatePayload(
    val habitId: String,
    val isCompleted: Boolean,
    val timestamp: Long
)
