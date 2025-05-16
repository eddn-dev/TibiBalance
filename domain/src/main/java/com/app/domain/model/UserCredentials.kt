package com.app.domain.model

import kotlinx.serialization.Serializable

/** Credenciales básicas de usuario para iniciar sesión. */
@Serializable
data class UserCredentials(
    val email: String,
    val password: String
)