package com.app.tibibalance.ui.screens.auth.forgot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.usecase.auth.SendResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Patterns                            // para validar e-mail

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendResetUC: SendResetPasswordUseCase
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    private val _ui = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val ui: StateFlow<ForgotPasswordUiState> = _ui

    fun onEmailChange(v: String) { email = v.trim() }

    fun sendResetLink() {
        if (email.isBlank()) {
            _ui.value = ForgotPasswordUiState.Error("Escribe tu correo")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _ui.value = ForgotPasswordUiState.Error("Correo inválido")
            return
        }

        viewModelScope.launch {
            _ui.value = ForgotPasswordUiState.Loading
            when (val r = sendResetUC(email)) {
                is AuthResult.Success -> _ui.value = ForgotPasswordUiState.Success
                is AuthResult.Error   -> _ui.value = mapError(r.error)
            }
        }
    }

    fun clearStatus() { _ui.value = ForgotPasswordUiState.Idle }

    /* ---- Error mapper ---- */
    private fun mapError(e: AuthError): ForgotPasswordUiState = when (e) {
        AuthError.UserNotFound ->
            ForgotPasswordUiState.Error("Correo no registrado")
        AuthError.Network, AuthError.Timeout ->
            ForgotPasswordUiState.Error("Sin conexión. Intenta más tarde")
        else ->
            ForgotPasswordUiState.Error("Error: ${e::class.simpleName}")
    }
}
