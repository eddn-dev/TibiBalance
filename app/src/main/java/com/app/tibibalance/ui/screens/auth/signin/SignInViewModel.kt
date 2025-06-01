/* ui/screens/auth/signin/SignInViewModel.kt */
package com.app.tibibalance.ui.screens.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SignInUseCase
import com.app.tibibalance.ui.components.utils.mapAuthErrorToMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @file        SignInViewModel.kt
 * @ingroup     ui_screens_auth_signin
 * @brief       ViewModel encargado del flujo de inicio de sesión (email y Google).
 *
 * @details
 *  - Consume [SignInUseCase] y [GoogleSignInUseCase] que devuelven [AuthResult].
 *  - Mantiene el estado de UI en un [MutableStateFlow] expuesto como [StateFlow].
 *  - Traduce todos los [AuthError] de dominio a [SignInUiState.Error] usando [mapAuthErrorToMessage].
 *
 * @see SignInUiState Estados posibles de la UI en esta pantalla.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val googleUseCase: GoogleSignInUseCase
) : ViewModel() {

    /** Estado observable para la pantalla de inicio de sesión. */
    private val _ui = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val ui: StateFlow<SignInUiState> = _ui

    /* ──────────────── E-mail / contraseña ──────────────── */

    /**
     * Inicia el proceso de login con email y contraseña.
     *
     * 1) Valida localmente los campos: correo no vacío/formato válido, contraseña no vacía.
     * 2) Emite [SignInUiState.Loading] mientras espera la respuesta de [signInUseCase].
     * 3) Si el resultado es exitoso, emite [SignInUiState.Success(res.data)] donde res.data es Boolean.
     * 4) Si hay error, traduce el [AuthError] en un mensaje con emojis usando [mapAuthErrorToMessage]
     *    y emite [SignInUiState.Error(mensaje)].
     *
     * @param email Correo ingresado por el usuario.
     * @param pass  Contraseña ingresada por el usuario.
     */
    fun signIn(email: String, pass: String) = viewModelScope.launch {
        // 1) Validación cliente
        val emailErr = when {
            email.isBlank() -> "✏️ El correo es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                -> "📧 Formato de correo inválido"
            else -> null
        }
        val passErr = if (pass.isBlank()) "✏️ La contraseña es obligatoria" else null

        if (emailErr != null || passErr != null) {
            _ui.value = SignInUiState.FieldError(emailError = emailErr, passError = passErr)
            return@launch
        }

        // 2) Lógica de negocio
        _ui.value = SignInUiState.Loading
        when (val res = signInUseCase(UserCredentials(email.trim(), pass))) {
            is AuthResult.Success -> {
                // Aquí res.data es un Boolean que indica si el correo está verificado
                _ui.value = SignInUiState.Success(res.data)
            }
            is AuthResult.Error -> {
                // 3) Usamos el mapeador global para todos los AuthError
                val mensaje = mapAuthErrorToMessage(res.error)
                _ui.value = SignInUiState.Error(mensaje)
            }
        }
    }

    /* ──────────────── Google One-Tap ───────────────────── */

    /**
     * Completa el flujo One-Tap de Google y traduce errores.
     *
     * 1) Emite [SignInUiState.Loading] mientras espera la respuesta de [googleUseCase].
     * 2) Si es exitoso, emite [SignInUiState.Success(true)] asumiendo que Google regresa verificado = true.
     * 3) Si hay error, traduce el [AuthError] a mensaje usando [mapAuthErrorToMessage]
     *    y emite [SignInUiState.Error(mensaje)].
     *
     * @param idToken Token JWT recibido de Google One-Tap.
     */
    fun finishGoogleSignIn(idToken: String) = viewModelScope.launch {
        _ui.value = SignInUiState.Loading

        when (val res = googleUseCase(idToken)) {
            is AuthResult.Success -> {
                // Se asume que toda autenticación con Google implica usuario verificado
                _ui.value = SignInUiState.Success(true)
            }
            is AuthResult.Error -> {
                val mensaje = mapAuthErrorToMessage(res.error)
                _ui.value = SignInUiState.Error(mensaje)
            }
        }
    }

    /** Reinicia el estado para descartar errores ya mostrados. */
    fun consumeError() {
        _ui.value = SignInUiState.Idle
    }
}
