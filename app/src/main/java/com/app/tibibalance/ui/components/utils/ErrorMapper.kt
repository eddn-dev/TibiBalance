/* ui/components/utils/ErrorMapper.kt */
package com.app.tibibalance.ui.components.utils

import com.app.domain.error.AuthError

/**
 * Convierte un AuthError en un mensaje legible para el usuario, con emojis.
 */
fun mapAuthErrorToMessage(error: AuthError): String {
    return when (error) {
        AuthError.Network ->
            "📡 Sin conexión. Intenta más tarde."

        AuthError.Timeout ->
            "⌛ Tiempo de espera agotado. Revisa tu conexión."

        AuthError.UserNotFound ->
            "📧 Correo no registrado."

        AuthError.InvalidCredentials ->
            "🔑 Correo o contraseña incorrectos."

        AuthError.EmailAlreadyUsed ->
            "✉️ Este correo ya está en uso."

        AuthError.WeakPassword ->
            "🔒 La contraseña es demasiado débil."

        AuthError.EmailNotVerified ->
            "✔️ Tu correo no ha sido verificado. Revisa tu bandeja de entrada."

        is AuthError.Unknown -> {
            val causa = error.cause?.message ?: "Error desconocido"
            "❓ Error desconocido: $causa"
        }
    }
}
