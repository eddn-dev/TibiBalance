/**
 * @file    HabitDto.kt
 * @ingroup data_remote_model
 * @brief   DTO serializable de [Habit] para Cloud Firestore.
 *
 * Puntos clave:
 *  • Guardamos enums / data-classes tal-cual; Firestore los soporta como mapas anidados.
 *  • Las marcas temporales se almacenan como Firebase [Timestamp] y se convierten
 *    a/from kotlinx.datetime [Instant].
 *  • `category` se persiste como String para evitar reflexión extra en Firestore.
 *
 */
package com.app.data.remote.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.common.SyncMeta
import com.app.domain.config.*
import com.app.domain.entities.Habit
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.HabitId
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant

@Suppress("unused") // Firestore usa reflexión en el constructor sin args
data class HabitDto(

    /* ─────────── Identidad ─────────────────────────────── */

    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    /* ─────────── Datos básicos ─────────────────────────── */

    var name        : String = "",
    var description : String = "",
    var category    : String = HabitCategory.SALUD.name, // se almacena como texto
    var icon        : String = "ic_favorite",

    /* ─────────── Configuraciones ───────────────────────── */

    var session     : Session      = Session(),
    var repeat      : Repeat       = Repeat.Daily(),
    var period      : Period       = Period(),
    var notifConfig : NotifConfig  = NotifConfig(),
    var challenge   : ChallengeConfig? = null,

    /* ─────────── Metadatos / banderas ──────────────────── */

    var isBuiltIn   : Boolean   = false,
    var createdAt   : Timestamp? = null,
    var updatedAt   : Timestamp? = null,
    var deletedAt   : Timestamp? = null,
    var pendingSync : Boolean    = false
) {

    /* ────────── Conversión a dominio ──────────────────── */

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(): Habit = Habit(
        id          = HabitId(id),
        name        = name,
        description = description,
        category    = runCatching { HabitCategory.valueOf(category) }
            .getOrElse { HabitCategory.SALUD },
        icon        = icon,
        session     = session,
        repeat      = repeat,
        period      = period,
        notifConfig = notifConfig,
        challenge   = challenge,
        isBuiltIn   = isBuiltIn,
        meta        = SyncMeta(
            createdAt   = createdAt?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
            updatedAt   = updatedAt?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
            deletedAt   = deletedAt?.toInstant()?.toKotlinInstant(),
            pendingSync = pendingSync
        )
    )

    /* ────────── Conversión desde dominio ──────────────── */

    companion object {
        fun fromDomain(h: Habit) = HabitDto(
            id          = h.id.raw,
            name        = h.name,
            description = h.description,
            category    = h.category.name,
            icon        = h.icon,
            session     = h.session,
            repeat      = h.repeat,
            period      = h.period,
            notifConfig = h.notifConfig,
            challenge   = h.challenge,
            isBuiltIn   = h.isBuiltIn,
            createdAt   = h.meta.createdAt.toTimestamp(),
            updatedAt   = h.meta.updatedAt.toTimestamp(),
            deletedAt   = h.meta.deletedAt.toTimestamp(),
            pendingSync = h.meta.pendingSync
        )
    }
}