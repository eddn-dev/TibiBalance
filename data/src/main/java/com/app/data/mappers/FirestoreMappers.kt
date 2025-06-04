package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.common.SyncMeta
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
    "notifEmotion"     to notifEmotion,
    "language"         to language,
    "accessibilityTTS" to accessibilityTTS
)

@RequiresApi(Build.VERSION_CODES.O)
fun SyncMeta.toFirestoreMap() = mapOf(
    "createdAt"  to createdAt.toTimestamp(),
    "updatedAt"  to updatedAt.toTimestamp(),
    "deletedAt"  to deletedAt?.toTimestamp(),
    "pendingSync" to pendingSync
)

@RequiresApi(Build.VERSION_CODES.O)
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
    settings    = (get("settings") as Map<*, *>).run {
        UserSettings(
            theme            = ThemeMode.valueOf(get("theme") as String),
            notifGlobal      = get("notifGlobal") as Boolean,
            notifEmotion     = (get("notifEmotion") as? Boolean) != false,
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

@RequiresApi(Build.VERSION_CODES.O)
fun DocumentSnapshot.toUser(): User? = runCatching {

    /* ---------- helpers ---------- */
    fun Any?.toLocalDate(): LocalDate = when (this) {
        is Timestamp -> this.toDate().toInstant().toKotlinInstant()
            .toLocalDateTime(TimeZone.UTC).date
        is Date      -> this.toInstant().toKotlinInstant()
            .toLocalDateTime(TimeZone.UTC).date
        is String    -> LocalDate.parse(this)               // “2000-01-01”
        else         -> error("birthDate type unsupported: $this")
    }

    fun Any?.toInstant(): Instant = when (this) {
        is Timestamp -> this.toDate().toInstant().toKotlinInstant()
        is Date      -> this.toInstant().toKotlinInstant()
        is String    -> Instant.parse(this)                 // ISO-8601
        else         -> error("instant type unsupported: $this")
    }

    val data = data ?: error("document has no data")

    return User(
        uid         = getString("uid") ?: id,
        email       = getString("email") ?: "",
        displayName = getString("displayName"),
        photoUrl    = getString("photoUrl"),
        birthDate   = data["birthDate"].toLocalDate(),

        settings    = (data["settings"] as? Map<*, *>)?.let { s ->
            UserSettings(
                theme            = runCatching { ThemeMode.valueOf(s["theme"] as String) }
                    .getOrDefault(ThemeMode.SYSTEM),
                notifGlobal      = (s["notifGlobal"] as? Boolean) != false,
                notifEmotion     = (s["notifEmotion"] as? Boolean) != false,
                language         = s["language"] as? String ?: "es",
                accessibilityTTS = s["accessibilityTTS"] as? Boolean ?: false
            )
        } ?: UserSettings(),

        meta        = (data["meta"] as? Map<*, *>)?.let { m ->
            SyncMeta(
                createdAt   = m["createdAt"].toInstant(),
                updatedAt   = m["updatedAt"].toInstant(),
                deletedAt   = m["deletedAt"]?.let { it.toInstant() },
                pendingSync = m["pendingSync"] as? Boolean ?: false
            )
        } ?: SyncMeta()
    )
}.onFailure {  }.getOrNull()