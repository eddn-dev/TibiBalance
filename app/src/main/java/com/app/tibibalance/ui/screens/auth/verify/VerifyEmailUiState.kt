package com.app.tibibalance.ui.screens.auth.verify

sealed interface VerifyEmailUiState {
    object Idle : VerifyEmailUiState
    object Loading : VerifyEmailUiState
    data class Success(val message: String, val goHome: Boolean = false) : VerifyEmailUiState
    data class Error(val message: String) : VerifyEmailUiState
    object SignedOut : VerifyEmailUiState
}
