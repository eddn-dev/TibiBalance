package com.app.domain.error

// :domain/com/app/domain/error/AuthResult.kt
sealed class AuthResult<out T> {
    data class Success<T>(val data: T)   : AuthResult<T>()
    data class Error  (val error: AuthError) : AuthResult<Nothing>()
}