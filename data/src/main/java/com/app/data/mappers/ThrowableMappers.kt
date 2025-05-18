package com.app.data.mappers

import com.app.domain.error.AuthError
import com.google.firebase.auth.FirebaseAuthException
import java.io.IOException
import java.net.SocketTimeoutException

// :data/mappers/ThrowableMappers.kt
internal fun Throwable.toAuthError(): AuthError = when (this) {
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use" -> AuthError.EmailAlreadyUsed
        "ERROR_WEAK_PASSWORD",       "weak-password"        -> AuthError.WeakPassword
        "ERROR_WRONG_PASSWORD",      "wrong-password"       -> AuthError.InvalidCredentials
        "ERROR_USER_NOT_FOUND",      "user-not-found"       -> AuthError.UserNotFound
        else                                                 -> AuthError.Unknown(this)
    }
    is SocketTimeoutException -> AuthError.Timeout
    is IOException           -> AuthError.Network
    else                     -> AuthError.Unknown(this)
}
