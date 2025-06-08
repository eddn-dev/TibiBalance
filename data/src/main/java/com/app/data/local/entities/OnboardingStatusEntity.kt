package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.DateTimeConverters
import com.app.domain.common.SyncMeta
import kotlinx.datetime.Instant

/**
 * Nota: clave primaria = uid para mantener 1-a-1 con usuario.
 */
@Entity(tableName = "onboarding_status")
@TypeConverters(DateTimeConverters::class)
data class OnboardingStatusEntity(
    @PrimaryKey                      val uid: String,
    val hasCompletedTutorial         : Boolean,
    val tutorialCompleted            : Boolean,
    val legalAccepted                : Boolean,
    val permissionsAsked             : Boolean,
    val completedAt                  : Instant?,
    @Embedded(prefix = "m_")      val meta: SyncMeta
)