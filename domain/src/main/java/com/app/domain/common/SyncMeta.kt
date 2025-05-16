// com/app/domain/common/SyncMeta.kt
package com.app.domain.common

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * @file    SyncMeta.kt
 * @ingroup domain_common
 * @brief   Metadatos de sincronización LWW para Firestore/Room.
 */
@Serializable
data class SyncMeta(
    val createdAt  : Instant = Instant.DISTANT_PAST,
    val updatedAt  : Instant = Instant.DISTANT_PAST,
    val deletedAt  : Instant? = null,
    val pendingSync: Boolean = false
)
