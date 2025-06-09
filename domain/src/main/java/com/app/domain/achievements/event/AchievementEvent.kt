package com.app.domain.achievements.event

import com.app.domain.enums.Emotion
import com.app.domain.enums.HabitCategory
import kotlinx.datetime.LocalDate

// :domain/achievements/event/AchievementEvent.kt
sealed interface AchievementEvent {
    data class HabitAdded(
        val category: HabitCategory,
        val isChallenge: Boolean
    ) : AchievementEvent

    data class EmotionLogged(
        val date : LocalDate,
        val mood : Emotion
    ) : AchievementEvent

    data class ProfileUpdated(val changedPhoto: Boolean) : AchievementEvent
    object NotifCustomized : AchievementEvent
}
