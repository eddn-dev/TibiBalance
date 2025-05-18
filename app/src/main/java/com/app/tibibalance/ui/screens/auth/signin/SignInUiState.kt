// ui/screens/auth/signin/SignInUiState.kt
package com.app.tibibalance.ui.screens.auth.signin

/** Estados posibles de la UI de inicio de sesión. */
sealed interface SignInUiState {
    data object Idle    : SignInUiState
    data object Loading : SignInUiState

    data class FieldError(
        val emailError: String? = null,
        val passError : String? = null,
    ) : SignInUiState

    data class Error(val message: String)        : SignInUiState
    data class Success(val verified: Boolean)    : SignInUiState
}
