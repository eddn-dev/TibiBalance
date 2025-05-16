package com.app.domain.entities

import com.app.domain.common.SyncMeta
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * @file    User.kt
 * @ingroup domain_entities
 * @brief   Perfil principal almacenado en Firestore `users/{uid}`.
 */
@Serializable
data class User(
    val uid         : String,
    val email       : String,
    val displayName : String?       = null,
    val photoUrl    : String?       = null,
    val birthDate   : LocalDate,
    val settings    : UserSettings  = UserSettings(),
    val meta        : SyncMeta      = SyncMeta()
)
