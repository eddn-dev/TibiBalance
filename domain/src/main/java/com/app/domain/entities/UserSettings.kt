package com.app.domain.entities

import com.app.domain.enums.ThemeMode
import kotlinx.serialization.Serializable

/**
 * @file    UserSettings.kt
 * @ingroup domain_entities
 * @brief   Preferencias globales del usuario.
 */
@Serializable
data class UserSettings(
    val theme            : ThemeMode = ThemeMode.SYSTEM,
    val notifGlobal      : Boolean   = true,
    val notifEmotion     : Boolean   = true,
    val language         : String    = "es",
    val accessibilityTTS : Boolean   = false
)
