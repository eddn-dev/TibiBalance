// com/app/domain/entities/Habit.kt
package com.app.domain.entities

import com.app.domain.common.SyncMeta
import com.app.domain.config.*
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.HabitId
import kotlinx.serialization.Serializable

/**
 * Entidad de dominio que representa un hábito del usuario.
 */
@Serializable
data class Habit(
    val id         : HabitId,
    val name       : String,
    val description: String               = "",
    val category   : HabitCategory        = HabitCategory.SALUD,
    val icon       : String               = "ic_favorite",
    val session    : Session              = Session(),
    val repeat     : Repeat               = Repeat.Daily(),
    val period     : Period               = Period(),
    val notifConfig: NotifConfig          = NotifConfig(),
    val challenge  : ChallengeConfig?     = null,
    val isBuiltIn  : Boolean              = false,
    val meta       : SyncMeta             = SyncMeta()
)