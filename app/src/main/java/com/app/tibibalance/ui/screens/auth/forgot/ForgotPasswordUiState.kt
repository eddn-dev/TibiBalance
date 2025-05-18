package com.app.tibibalance.ui.screens.auth.forgot

sealed interface ForgotPasswordUiState {
    data object Idle     : ForgotPasswordUiState
    data object Loading  : ForgotPasswordUiState
    data object Success  : ForgotPasswordUiState        // correo enviado
    data class Error(val message: String) : ForgotPasswordUiState
}
