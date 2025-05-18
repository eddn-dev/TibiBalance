/**
 * @file    HabitDto.kt
 * @ingroup data_remote_model
 * @brief   Representación serializable para Firestore.
 *
 * Nota:  Puedes usar @PropertyName si prefieres camelCase en Firestore.
 */
package com.app.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class HabitDto(
    @get:PropertyName("id")             @set:PropertyName("id")
    var id: String = "",

    var name: String = "",
    var description: String = "",
    var category: String = "SALUD",
    var icon: String = "ic_favorite",

    /* Campos complejos serializados a JSON texto                   */
    var session: String = "{}",         // JSON-string
    var repeat: String = "{}",
    var period: String = "{}",
    var notifConfig: String = "{}",
    var challenge: String? = null,

    /* Sync LWW                                                    */
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var deletedAt: Timestamp? = null
)
