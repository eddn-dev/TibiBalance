package com.app.tibibalance.ui.screens.auth.signup

sealed interface SignUpUiState {
    object Idle                    : SignUpUiState
    object Loading                 : SignUpUiState

    data class FieldError(
        val userNameError  : String? = null,
        val birthDateError : String? = null,
        val emailError     : String? = null,
        val pass1Error     : String? = null,
        val pass2Error     : String? = null,
    ) : SignUpUiState

    data class Error(val message: String) : SignUpUiState        // global
    data class Success(val email: String)  : SignUpUiState       // vía e-mail
    object GoogleSuccess                  : SignUpUiState        // vía Google
}
