package com.app.domain.entities

import com.app.domain.common.SyncMeta
import com.app.domain.ids.AchievementId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Modelo de dominio para un logro.
 */
@Serializable
data class Achievement(
    val id         : AchievementId,
    val name       : String,
    val description: String,
    val progress   : Int           = 0,      // 0-100
    val unlocked   : Boolean       = false,
    val unlockDate : Instant?      = null,
    val meta       : SyncMeta      = SyncMeta()
)
