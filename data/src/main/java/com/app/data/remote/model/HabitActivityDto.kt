/**
 * @file    HabitActivityDto.kt
 * @ingroup data_remote_model
 * @brief   DTO para serializar/deserializar actividades con Firestore/REST.
 *
 *  • Representa TODOS los campos de dominio, pero usa tipos simples:
 *      - Fechas  ➜ ISO‐8601 String  ("2025-06-02")
 *      - Horas   ➜ ISO‐8601 String  ("08:00")
 *      - Instant ➜ ISO String       ("2025-06-02T08:00:00Z")
 *      - Enums   ➜ name()           ("COMPLETED")
 *
 *  • `SyncMetaDto` es anidado para no duplicar lógica serializer.
 */
package com.app.data.remote.model

import com.app.domain.common.SyncMeta
import com.app.domain.entities.*
import com.app.domain.enums.ActivityStatus
import com.app.domain.enums.SessionUnit
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

/* ──────────────── DTOs ──────────────── */

@Serializable
data class SyncMetaDto(
    val createdAt   : String,
    val updatedAt   : String,
    val deletedAt   : String? = null,
    val pendingSync : Boolean
) {
    fun toDomain() = SyncMeta(
        createdAt   = Instant.parse(createdAt),
        updatedAt   = Instant.parse(updatedAt),
        deletedAt   = deletedAt?.let(Instant::parse),
        pendingSync = pendingSync
    )
    companion object {
        fun fromDomain(m: SyncMeta) = SyncMetaDto(
            createdAt   = m.createdAt.toString(),
            updatedAt   = m.updatedAt.toString(),
            deletedAt   = m.deletedAt?.toString(),
            pendingSync = m.pendingSync
        )
    }
}

@Serializable
data class HabitActivityDto(
    val id           : String,
    val habitId      : String,
    val activityDate : String,        // yyyy-MM-dd
    val scheduledTime: String? = null,// HH:mm
    val status       : String,
    val targetQty    : Int?   = null,
    val recordedQty  : Int?   = null,
    val sessionUnit  : String? = null,
    val loggedAt     : String? = null,
    val generatedAt  : String,
    val meta         : SyncMetaDto
) {

    /* ──────────────── DTO → Domain ──────────────── */
    fun toDomain() = HabitActivity(
        id           = ActivityId(id),
        habitId      = HabitId(habitId),
        activityDate = LocalDate.parse(activityDate),
        scheduledTime= scheduledTime?.let(LocalTime::parse)?: LocalTime(0,0),
        status       = ActivityStatus.valueOf(status),
        targetQty    = targetQty,
        recordedQty  = recordedQty,
        sessionUnit  = sessionUnit?.let { SessionUnit.valueOf(it) },
        loggedAt     = loggedAt?.let(Instant::parse),
        generatedAt  = Instant.parse(generatedAt),
        meta         = meta.toDomain()
    )

    /* ──────────────── Domain → DTO ──────────────── */
    companion object {
        fun fromDomain(a: HabitActivity) = HabitActivityDto(
            id           = a.id.raw,
            habitId      = a.habitId.raw,
            activityDate = a.activityDate.toString(),
            scheduledTime= a.scheduledTime?.toString(),
            status       = a.status.name,
            targetQty    = a.targetQty,
            recordedQty  = a.recordedQty,
            sessionUnit  = a.sessionUnit?.name,
            loggedAt     = a.loggedAt?.toString(),
            generatedAt  = a.generatedAt.toString(),
            meta         = SyncMetaDto.fromDomain(a.meta)
        )
    }
}
