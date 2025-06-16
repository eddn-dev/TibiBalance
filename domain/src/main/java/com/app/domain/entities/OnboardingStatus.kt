package com.app.domain.entities

import com.app.domain.common.SyncMeta
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * @file    OnboardingStatus.kt
 * @ingroup domain_entities
 * @brief   Progreso del flujo inicial (`onboarding`).
 */
@Serializable
data class OnboardingStatus(
    /** Flag that marks if the interactive tutorial has been completed. */
    val hasCompletedTutorial: Boolean = false,
    val tutorialCompleted : Boolean = false,
    val legalAccepted     : Boolean = false,
    val permissionsAsked  : Boolean = false,
    val completedAt       : Instant? = null,
    val meta              : SyncMeta = SyncMeta(),

    val hasSeenTutorial_HomeScreenMain: Boolean = false,
    val hasSeenTutorial_HabitsScreen: Boolean = false,
    val hasSeenTutorial_EmotionsScreen: Boolean = false,
    val hasSeenTutorial_HomeScreenStats: Boolean = false,
    val hasSeenTutorial_SettingsScreen: Boolean = false
)
