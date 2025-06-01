/* ui/screens/auth/forgot/ForgotPasswordViewModel.kt */
package com.app.tibibalance.ui.screens.auth.forgot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.usecase.auth.SendResetPasswordUseCase
import com.app.tibibalance.ui.components.utils.mapAuthErrorToMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import javax.inject.Inject

/**
 * @brief ViewModel para la pantalla de recuperación de contraseña (ForgotPasswordScreen).
 *
 * - Mantiene el campo `email` y valida que no esté vacío ni mal formado.
 * - Invoca [SendResetPasswordUseCase] para enviar el enlace de recuperación.
 * - Convierte cualquier [AuthError] a una cadena legible usando [mapAuthErrorToMessage].
 * - Expone un flujo [ui] de tipo [ForgotPasswordUiState] (que ya está definido como sealed interface).
 *
 * @property sendResetUC Caso de uso que envía el correo de recuperación.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendResetUC: SendResetPasswordUseCase
) : ViewModel() {

    /** Correo ingresado por el usuario. */
    var email by mutableStateOf("")
        private set

    private val _ui = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val ui: StateFlow<ForgotPasswordUiState> = _ui

    /**
     * Se llama cada vez que el usuario modifica el campo de correo.
     * @param v Cadena ingresada en el campo de correo (se recorta espacios).
     */
    fun onEmailChange(v: String) {
        email = v.trim()
    }

    /**
     * Inicia el flujo para enviar el enlace de recuperación:
     * 1) Verifica localmente que `email` no esté vacío ni sea mal-formado.
     * 2) Cambia el estado a Loading.
     * 3) Llama a sendResetUC(email) y:
     *    - Si es AuthResult.Success, emite ForgotPasswordUiState.Success.
     *    - Si es AuthResult.Error, convierte el AuthError a texto con mapAuthErrorToMessage
     *      y emite ForgotPasswordUiState.Error(mensaje).
     */
    fun sendResetLink() {
        // 1) Validaciones de campo
        if (email.isBlank()) {
            _ui.value = ForgotPasswordUiState.Error("✏️ Escribe tu correo")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _ui.value = ForgotPasswordUiState.Error("📧 Correo inválido")
            return
        }

        // 2) Llamada al caso de uso
        viewModelScope.launch {
            _ui.value = ForgotPasswordUiState.Loading

            when (val result = sendResetUC(email)) {
                is AuthResult.Success -> {
                    _ui.value = ForgotPasswordUiState.Success
                }
                is AuthResult.Error -> {
                    // 3) Mapeamos el AuthError a un mensaje con emojis
                    val mensaje = mapAuthErrorToMessage(result.error)
                    _ui.value = ForgotPasswordUiState.Error(mensaje)
                }
            }
        }
    }

    /**
     * Restablece el estado de la UI a Idle.
     * Llamar desde la UI cuando el usuario cierre el modal.
     */
    fun clearStatus() {
        _ui.value = ForgotPasswordUiState.Idle
    }
}

