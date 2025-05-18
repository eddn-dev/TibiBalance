// :domain/src/main/java/com/app/domain/entities/HabitTemplate.kt
package com.app.domain.entities

import com.app.domain.enums.HabitCategory
import kotlinx.serialization.Serializable

@Serializable
data class HabitTemplate(
    val id        : String,           // firestore document id
    val name      : String,
    val icon      : String,
    val category  : HabitCategory,
    val formDraft : HabitForm         // the pre-filled HabitForm for the wizard
)
