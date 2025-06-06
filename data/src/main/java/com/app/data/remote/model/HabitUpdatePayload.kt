package com.app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class HabitUpdatePayload(
    val habitId: String,
    val isCompleted: Boolean,
    val timestamp: Long
)
