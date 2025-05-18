package com.app.tibibalance.ui.screens.auth.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.usecase.auth.CheckEmailVerifiedUseCase
import com.app.domain.usecase.auth.ResendVerificationUseCase
import com.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val resendUseCase : ResendVerificationUseCase,
    private val checkUseCase  : CheckEmailVerifiedUseCase,
    private val signOutRepo   : AuthRepository         // solo para signOut()
) : ViewModel() {

    private val _ui = MutableStateFlow<VerifyEmailUiState>(VerifyEmailUiState.Idle)
    val ui: StateFlow<VerifyEmailUiState> = _ui

    fun resend() = viewModelScope.launch {
        _ui.value = VerifyEmailUiState.Loading
        when (resendUseCase()) {
            is AuthResult.Success -> _ui.value = VerifyEmailUiState.Success("Correo reenviado")
            is AuthResult.Error   -> _ui.value = VerifyEmailUiState.Error("Error al reenviar")
        }
    }

    fun verify() = viewModelScope.launch {
        _ui.value = VerifyEmailUiState.Loading
        when (val r = checkUseCase()) {
            is AuthResult.Success -> {
                if (r.data)
                    _ui.value = VerifyEmailUiState.Success("¡Verificado!", goHome = true)
                else _ui.value = VerifyEmailUiState.Error("Aún no está verificado")  // usuario aún no clicó el enlace
            }
            is AuthResult.Error   -> _ui.value = mapError(r.error)
        }
    }

    fun signOut() = viewModelScope.launch {
        signOutRepo.signOut()
        _ui.value = VerifyEmailUiState.SignedOut
    }

    fun clear() { if (_ui.value !is VerifyEmailUiState.Loading) _ui.value = VerifyEmailUiState.Idle }

    private fun mapError(e: AuthError) = when (e) {
        AuthError.Network, AuthError.Timeout -> VerifyEmailUiState.Error("Sin conexión")
        else -> VerifyEmailUiState.Error("Error desconocido")
    }
}
