/**
 * @file    BasicError.kt
 * @ingroup domain_error
 * @brief   Errores de validación del paso “Información básica”.
 */
package com.app.domain.error

/**
 * Conjunto cerrado de las validaciones que hoy se controlan en el
 * paso *Basic-Info* del asistente.
 *
 * >  Tener la jerarquía en *domain* evita referencias cruzadas
 * >  desde ViewModels/UseCases a la capa de presentación.
 */
sealed interface BasicError {
    /** El campo *name* está vacío. */
    data object NameRequired : BasicError
}
