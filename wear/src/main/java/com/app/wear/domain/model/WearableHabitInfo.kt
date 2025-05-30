package com.app.wear.domain.model

data class WearableHabitInfo(
    val id: String, // ID del hábito para referencia
    val name: String,
    val isCompletedToday: Boolean
    // Otros campos relevantes para la UI del wearable, si los hay
)
