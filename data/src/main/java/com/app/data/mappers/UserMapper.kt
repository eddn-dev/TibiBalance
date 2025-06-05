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

    /** Entity → Domain */
    fun UserEntity.toDomain(): User = User(
        uid         = uid,
        email       = email,
        displayName = displayName,
        photoUrl    = photoUrl,
        birthDate   = birthDate,
        settings    = UserSettings(
            theme            = settingsTheme,
            notifGlobal      = settingsNotif,
            notifEmotion     = settingsEmotion,
            language         = settingsLang,
            accessibilityTTS = settingsTTS
        ),
        meta        = meta
    )

    /** Domain → Entity */
    fun User.toEntity(): UserEntity = UserEntity(
        uid           = uid,
        email         = email,
        displayName   = displayName,
        photoUrl      = photoUrl,
        birthDate     = birthDate,
        settingsTheme = settings.theme,
        settingsNotif = settings.notifGlobal,
        settingsLang  = settings.language,
        settingsTTS   = settings.accessibilityTTS,
        settingsEmotion =settings.notifEmotion,
        meta          = meta
    )
}
