/**
 * @file    OnboardingMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión OnboardingStatusEntity ↔ OnboardingStatus (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.OnboardingStatusEntity
import com.app.domain.entities.OnboardingStatus

object OnboardingMappers {

    /** Entity → Domain */
    fun OnboardingStatusEntity.toDomain(): OnboardingStatus = OnboardingStatus(
        hasCompletedTutorial = hasCompletedTutorial,
        tutorialCompleted = tutorialCompleted,
        legalAccepted     = legalAccepted,
        permissionsAsked  = permissionsAsked,
        completedAt       = completedAt,
        meta              = meta,
        hasSeenTutorial_HomeScreenMain  = hasSeenTutorial_HomeScreenMain,
        hasSeenTutorial_HomeScreenStats = hasSeenTutorial_HomeScreenStats,
        hasSeenTutorial_HabitsScreen    = hasSeenTutorial_HabitsScreen,
        hasSeenTutorial_EmotionsScreen  = hasSeenTutorial_EmotionsScreen,
        hasSeenTutorial_SettingsScreen  = hasSeenTutorial_SettingsScreen
    )

    /** Domain → Entity */
    fun OnboardingStatus.toEntity(uid: String): OnboardingStatusEntity =
        OnboardingStatusEntity(
            uid               = uid,
            hasCompletedTutorial = hasCompletedTutorial,
            tutorialCompleted = tutorialCompleted,
            legalAccepted     = legalAccepted,
            permissionsAsked  = permissionsAsked,
            completedAt       = completedAt,
            meta              = meta,
            hasSeenTutorial_HomeScreenMain  = hasSeenTutorial_HomeScreenMain,
            hasSeenTutorial_HomeScreenStats = hasSeenTutorial_HomeScreenStats,
            hasSeenTutorial_HabitsScreen    = hasSeenTutorial_HabitsScreen,
            hasSeenTutorial_EmotionsScreen  = hasSeenTutorial_EmotionsScreen,
            hasSeenTutorial_SettingsScreen  = hasSeenTutorial_SettingsScreen
        )
}
