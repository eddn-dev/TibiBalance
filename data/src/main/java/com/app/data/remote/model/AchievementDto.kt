/**
 * @file    AchievementDto.kt
 * @ingroup data_remote_model
 */
package com.app.data.remote.model

import com.app.data.util.safeTimestamp
import com.app.domain.common.SyncMeta
import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId
import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.Date                     // ← import explícito

/* ───────────────────────── DTO ─────────────────────────── */

@Serializable
data class AchievementDto(
    val id          : String   = "",
    val name        : String   = "",
    val description : String   = "",
    val progress    : Int      = 0,
    val unlock      : Boolean  = false,
    val unlockDate  : Instant? = null,
    val updatedAt   : Instant  = Instant.DISTANT_PAST
)

/* ─────────────── Domain ↔ DTO mappers ───────────────────── */

fun Achievement.toDto() = AchievementDto(
    id          = id.raw,
    name        = name,
    description = description,
    progress    = progress,
    unlock      = unlocked,
    unlockDate  = unlockDate,
    updatedAt   = meta.updatedAt
)

fun AchievementDto.toDomain() = Achievement(
    id          = AchievementId(id),
    name        = name,
    description = description,
    progress    = progress,
    unlocked    = unlock,
    unlockDate  = unlockDate,
    meta        = SyncMeta(updatedAt = updatedAt)
)

/* ─────────────── Firestore helpers (optional) ───────────── */

/** DTO → Map para usar con `set()` / `update()` de Firestore. */
fun AchievementDto.toMap(): Map<String, Any?> = mapOf(
    "name"        to name,
    "description" to description,
    "progress"    to progress,
    "unlock"      to unlock,
    "unlockDate"  to unlockDate.safeTimestamp(),
    "updatedAt"   to updatedAt.safeTimestamp()
)


/**
 * Convierte el `data` de un documento Firestore a DTO.
 * Se pasa `doc.id` explícitamente porque el ID vive en la ruta, no en el payload.
 */
fun Map<String, Any?>.toAchievementDto(id: String): AchievementDto = AchievementDto(
    id          = id,
    name        = this["name"]        as? String ?: "",
    description = this["description"] as? String ?: "",
    progress    = (this["progress"]   as? Number ?: 0).toInt(),
    unlock      = this["unlock"]      as? Boolean ?: false,
    unlockDate  = (this["unlockDate"] as? Timestamp)?.toKInstant(),
    updatedAt   = (this["updatedAt"]  as? Timestamp)?.toKInstant() ?: Instant.DISTANT_PAST
)

/* ─────────────── Conversion helpers ─────────────────────── */

private fun Instant.toDate(): Date =
    Date(toEpochMilliseconds())

private fun Timestamp.toKInstant(): Instant =
    Instant.fromEpochMilliseconds(toDate().time)
