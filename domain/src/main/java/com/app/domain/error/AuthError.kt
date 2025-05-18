// :domain/com/app/domain/error/AuthError.kt
package com.app.domain.error

sealed interface AuthError {
    object Network            : AuthError
    object Timeout            : AuthError
    object InvalidCredentials : AuthError
    object UserNotFound       : AuthError
    object EmailAlreadyUsed   : AuthError
    object WeakPassword       : AuthError
    object EmailNotVerified   : AuthError
    data class Unknown(val cause: Throwable) : AuthError
}

