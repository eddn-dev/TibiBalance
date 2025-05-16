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
        tutorialCompleted = tutorialCompleted,
        legalAccepted     = legalAccepted,
        permissionsAsked  = permissionsAsked,
        completedAt       = completedAt,
        meta              = meta
    )

    /** Domain → Entity */
    fun OnboardingStatus.toEntity(uid: String): OnboardingStatusEntity =
        OnboardingStatusEntity(
            uid               = uid,
            tutorialCompleted = tutorialCompleted,
            legalAccepted     = legalAccepted,
            permissionsAsked  = permissionsAsked,
            completedAt       = completedAt,
            meta              = meta
        )
}
