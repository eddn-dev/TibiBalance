package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.common.SyncMeta
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date

/* ---------- conversiones básicas ---------- */
fun LocalDate.toFirestoreDate(): Date =
    Date(atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())      //   OK para Firestore dates

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.toTimestamp(): Timestamp =
    Timestamp(this.toJavaInstant())

/* ---------- objetos a Map ---------- */
fun UserSettings.toFirestoreMap() = mapOf(
    "theme"            to theme.name,
    "notifGlobal"      to notifGlobal,
    "language"         to language,
    "accessibilityTTS" to accessibilityTTS
)

fun SyncMeta.toFirestoreMap() = mapOf(
    "createdAt"  to createdAt.toTimestamp(),
    "updatedAt"  to updatedAt.toTimestamp(),
    "deletedAt"  to deletedAt?.toTimestamp(),
    "pendingSync" to pendingSync
)

fun User.toFirestoreMap() = mapOf(
    "uid"         to uid,
    "email"       to email,
    "displayName" to displayName,
    "photoUrl"    to photoUrl,
    "birthDate"   to birthDate.toFirestoreDate(),
    "settings"    to settings.toFirestoreMap(),
    "meta"        to meta.toFirestoreMap()
)

@RequiresApi(Build.VERSION_CODES.O)
fun Map<String,Any?>.toUser(): User = User(
    uid         = get("uid") as String,
    email       = get("email") as String,
    displayName = get("displayName") as? String,
    photoUrl    = get("photoUrl") as? String,
    birthDate   = (get("birthDate") as Timestamp).toDate()
        .toInstant().toKotlinInstant().toLocalDateTime(TimeZone.UTC).date,
    settings    = (get("settings") as Map<String,Any?>).run {
        UserSettings(
            theme            = ThemeMode.valueOf(get("theme") as String),
            notifGlobal      = get("notifGlobal") as Boolean,
            language         = get("language") as String,
            accessibilityTTS = get("accessibilityTTS") as Boolean
        )
    },
    meta        = (get("meta") as Map<*, *>).run {
        SyncMeta(
            createdAt   = (get("createdAt") as Timestamp).toInstant().toKotlinInstant(),
            updatedAt   = (get("updatedAt") as Timestamp).toInstant().toKotlinInstant(),
            deletedAt   = (get("deletedAt") as? Timestamp)?.toInstant()?.toKotlinInstant(),
            pendingSync = get("pendingSync") as Boolean
        )
    }
)
