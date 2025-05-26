package com.app.domain.util

/**
 * Valida la “fuerza” de la contraseña según reglas mínimas.
 *
 * @return *null* si cumple todos los requisitos; en otro caso un mensaje
 *         multilínea con los puntos faltantes.
 */
object PasswordValidator {

    /** Caracteres permitidos para el requisito de símbolo. */
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{};:'\",.<>/?"

    fun validateStrength(pwd: String): String? {
        val req = buildList {
            if (pwd.length < 8)           add("• Mínimo 8 caracteres")
            if (pwd.none(Char::isUpperCase)) add("• Al menos 1 mayúscula")
            if (pwd.none(Char::isLowerCase)) add("• Al menos 1 minúscula")
            if (pwd.none(Char::isDigit))     add("• Al menos 1 número")
            if (pwd.none { SYMBOLS.contains(it) })
                add("• Al menos 1 símbolo ($SYMBOLS)")
        }
        return if (req.isEmpty()) null
        else "La contraseña debe contener:\n" + req.joinToString("\n")
    }
}
