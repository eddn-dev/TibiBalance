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
    val tutorialCompleted : Boolean = false,
    val legalAccepted     : Boolean = false,
    val permissionsAsked  : Boolean = false,
    val hasCompletedTutorial: Boolean = false,
    val completedAt       : Instant? = null,
    val meta              : SyncMeta = SyncMeta()
)
