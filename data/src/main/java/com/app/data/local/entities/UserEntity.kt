package com.app.data.local.entities

import androidx.room.*
import com.app.data.local.converters.*
import com.app.domain.common.SyncMeta
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

@Entity(tableName = "users")
@TypeConverters(DateTimeConverters::class, EnumConverters::class)
data class UserEntity(
    @PrimaryKey                             val uid: String,
    val email                               : String,
    val displayName                         : String?,
    val photoUrl                            : String?,
    val birthDate                           : LocalDate,
    /* ── settings embebido ── */
    @ColumnInfo(name = "settings_theme")    val settingsTheme  : ThemeMode,
    @ColumnInfo(name = "settings_notif")    val settingsNotif  : Boolean,
    @ColumnInfo(name = "settings_lang")     val settingsLang   : String,
    @ColumnInfo(name = "settings_tts")      val settingsTTS    : Boolean,
    /* ── SyncMeta ── */
    @Embedded(prefix = "meta_")             val meta: SyncMeta
) {
    companion object {
        fun fromDomain(u: com.app.domain.entities.User) = UserEntity(
            uid             = u.uid,
            email           = u.email,
            displayName     = u.displayName,
            photoUrl        = u.photoUrl,
            birthDate       = u.birthDate,
            settingsTheme   = u.settings.theme,
            settingsNotif   = u.settings.notifGlobal,
            settingsLang    = u.settings.language,
            settingsTTS     = u.settings.accessibilityTTS,
            meta            = u.meta
        )
    }

    fun toDomain() = com.app.domain.entities.User(
        uid         = uid,
        email       = email,
        displayName = displayName,
        photoUrl    = photoUrl,
        birthDate   = birthDate,
        settings    = UserSettings(
            theme            = settingsTheme,
            notifGlobal      = settingsNotif,
            language         = settingsLang,
            accessibilityTTS = settingsTTS
        ),
        meta        = meta
    )
}
