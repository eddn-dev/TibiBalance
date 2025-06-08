/**
 * @file    UserMappers.kt
 * @ingroup data_mapper
 * @brief   Conversión UserEntity ↔ User (dominio).
 */
package com.app.data.mappers

import com.app.data.local.entities.UserEntity
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings

object UserMappers {

    fun UserEntity.toDomain() = User(
        uid         = uid,
        email       = email,
        displayName = displayName,
        photoUrl    = photoUrl,
        birthDate   = birthDate,
        settings    = UserSettings(
            theme            = settingsTheme,
            notifGlobal      = settingsNotif,
            notifEmotion     = settingsEmotion,
            notifEmotionTime = settingsEmotionTime,   // ← NUEVO
            language         = settingsLang,
            accessibilityTTS = settingsTTS
        ),
        meta        = meta
    )

    fun User.toEntity() = UserEntity(
        uid                  = uid,
        email                = email,
        displayName          = displayName,
        photoUrl             = photoUrl,
        birthDate            = birthDate,
        settingsTheme        = settings.theme,
        settingsNotif  = settings.notifGlobal,
        settingsEmotion = settings.notifEmotion,
        settingsEmotionTime  = settings.notifEmotionTime, // ← NUEVO
        settingsLang         = settings.language,
        settingsTTS          = settings.accessibilityTTS,
        meta                 = meta
    )

}
