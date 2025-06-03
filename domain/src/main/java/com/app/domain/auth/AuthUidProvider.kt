/* :domain/auth/AuthUidProvider.kt */
package com.app.domain.auth

/** Provee el UID del usuario en sesión sin exponer Firebase. */
fun interface AuthUidProvider { operator fun invoke(): String }
