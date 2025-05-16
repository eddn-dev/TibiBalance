// com/app/domain/config/ChallengeConfig.kt
package com.app.domain.config

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeConfig(
    val start         : Instant,
    val end           : Instant,
    val currentStreak : Int = 0,
    val totalSessions : Int = 0,
    val failed        : Boolean = false,
    val lastFailureAt : Instant? = null
)
