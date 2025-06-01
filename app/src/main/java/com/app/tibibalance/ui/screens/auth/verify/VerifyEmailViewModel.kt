/* ui/screens/auth/verify/VerifyEmailViewModel.kt */
package com.app.tibibalance.ui.screens.auth.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.usecase.auth.CheckEmailVerifiedUseCase
import com.app.domain.usecase.auth.ResendVerificationUseCase
import com.app.domain.usecase.auth.ResendEmailResult
import com.app.domain.usecase.auth.SignOutUseCase
import com.app.tibibalance.ui.components.utils.mapAuthErrorToMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @file        VerifyEmailViewModel.kt
 * @ingroup     ui_screens_auth_verify
 * @brief       ViewModel encargado del flujo de verificación de correo electrónico.
 *
 * @details
 *  - Consume [ResendVerificationUseCase], [CheckEmailVerifiedUseCase] y [SignOutUseCase].
 *  - Mantiene el estado de UI en un [MutableStateFlow] expuesto como [StateFlow], utilizando
 *    la interfaz selada [VerifyEmailUiState] para representar Idle, Loading, éxito, error y cierre de sesión.
 *  - Traduce los [AuthError] de dominio a [VerifyEmailUiState.Error] usando [mapAuthErrorToMessage],
 *    de modo que la capa Compose reaccione sin conocer detalles de infraestructura ni lógica de negocio.
 *
 * Ejemplos de flujo:
 *  1) Usuario solicita reenvío de correo de verificación → [resendUseCase].
 *  2) Usuario da clic en “Ya lo verifiqué” → [checkUseCase]; si no está verificado, se muestra mensaje.
 *  3) Usuario puede cerrar sesión → [signOut].
 *
 * @see VerifyEmailUiState Estados posibles de la UI para esta pantalla.
 */
@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val resendUseCase: ResendVerificationUseCase,
    private val checkUseCase: CheckEmailVerifiedUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    /** Estado interno mutable para exponer el estado de UI. */
    private val _ui = MutableStateFlow<VerifyEmailUiState>(VerifyEmailUiState.Idle)

    /** Estado inmutable observado por la capa Compose. */
    val ui: StateFlow<VerifyEmailUiState> = _ui

    /**
     * Envía nuevamente el correo de verificación.
     *
     * 1) Emite [VerifyEmailUiState.Loading] mientras espera la respuesta de [resendUseCase].
     * 2) Si [ResendEmailResult.Success], emite [VerifyEmailUiState.Success] con mensaje emoji.
     * 3) Si [ResendEmailResult.Failure], traduce el error de reenvío y emite [VerifyEmailUiState.Error].
     *
     * @param email Correo electrónico al que reenviar el enlace.
     */
    fun resend(email: String) = viewModelScope.launch {
        _ui.value = VerifyEmailUiState.Loading

        when (val result = resendUseCase(email)) {
            is ResendEmailResult.Success -> {
                _ui.value = VerifyEmailUiState.Success("✉️ Correo reenviado")
            }
            is ResendEmailResult.Failure -> {
                // En caso de fallo en reenvío, mostramos razón (sin usar mapAuthErrorToMessage porque no es AuthError)
                _ui.value = VerifyEmailUiState.Error("❌ Falló el reenvío: ${result.reason}")
            }
        }
    }

    /**
     * Verifica si el correo ya fue confirmado.
     *
     * 1) Emite [VerifyEmailUiState.Loading] mientras espera la respuesta de [checkUseCase].
     * 2) Si [AuthResult.Success] y data==true, emite [VerifyEmailUiState.Success] con flag goHome=true.
     * 3) Si data==false, emite [VerifyEmailUiState.Error] con mensaje emoji.
     * 4) Si [AuthResult.Error], traduce el [AuthError] a mensaje con [mapAuthErrorToMessage]
     *    y emite [VerifyEmailUiState.Error].
     */
    fun verify() = viewModelScope.launch {
        _ui.value = VerifyEmailUiState.Loading

        when (val r = checkUseCase()) {
            is AuthResult.Success -> {
                if (r.data) {
                    _ui.value = VerifyEmailUiState.Success("✅ ¡Verificado!", goHome = true)
                } else {
                    _ui.value = VerifyEmailUiState.Error("⌛ Aún no está verificado")
                }
            }
            is AuthResult.Error -> {
                val mensaje = mapAuthErrorToMessage(r.error)
                _ui.value = VerifyEmailUiState.Error(mensaje)
            }
        }
    }

    /**
     * Cierra sesión del usuario.
     *
     * 1) Llama a [signOutUseCase] para borrar credenciales de sesión.
     * 2) Emite [VerifyEmailUiState.SignedOut].
     */
    fun signOut() = viewModelScope.launch {
        signOutUseCase()
        _ui.value = VerifyEmailUiState.SignedOut
    }

    /**
     * Limpia el estado de UI a Idle, a menos que esté en Loading.
     * Úsalo desde la UI cuando se cierre un modal o se quiera resetear.
     */
    fun clear() {
        if (_ui.value !is VerifyEmailUiState.Loading) {
            _ui.value = VerifyEmailUiState.Idle
        }
    }

    /**
     * Traduce un [AuthError] de dominio a [VerifyEmailUiState.Error], usando emojis.
     *
     * @param e Error de autenticación.
     * @return Estado de UI con mensaje apropiado.
     */
    private fun mapError(e: AuthError): VerifyEmailUiState =
        VerifyEmailUiState.Error(mapAuthErrorToMessage(e))
}
