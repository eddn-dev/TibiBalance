package com.app.domain.model

/** Modela un resultado simple de autenticación. */
sealed interface AuthResult {
    data class Success(val uid: String) : AuthResult
    data class Error(val throwable: Throwable) : AuthResult
}