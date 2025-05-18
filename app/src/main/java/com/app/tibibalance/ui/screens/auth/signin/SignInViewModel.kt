/**
 * @file        SignInViewModel.kt
 * @ingroup     ui_screens_auth_signin
 * @brief       ViewModel encargado del flujo de inicio de sesión (email y Google).
 *
 * @details
 *  - Consume [SignInUseCase] y [GoogleSignInUseCase] que devuelven [AuthResult].
 *  - Mantiene el estado de UI en un [MutableStateFlow] expuesto como [StateFlow].
 *  - Traduce los [AuthError] de dominio a [SignInUiState] para que la vista
 *    reaccione sin conocer detalles de infraestructura.
 *
 * @author      Edd
 * @date        2025-05-17
 */
package com.app.tibibalance.ui.screens.auth.signin

import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SignInUseCase
import com.app.tibibalance.ui.screens.auth.signin.SignInUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase : SignInUseCase,
    private val googleUseCase : GoogleSignInUseCase
) : ViewModel() {

    /** Estado observable para la pantalla de inicio de sesión. */
    private val _ui = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val ui: StateFlow<SignInUiState> = _ui                 // exposición inmutable

    /* ──────────────── E-mail / contraseña ──────────────── */
    fun signIn(email: String, pass: String) = viewModelScope.launch {
        // ── 1) Validación cliente ────────────────────────────────────
        val emailErr = when {
            email.isBlank()                           -> "El correo es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                -> "Formato de correo inválido"
            else                                      -> null
        }
        val passErr  = if (pass.isBlank()) "La contraseña es obligatoria" else null

        if (emailErr != null || passErr != null) {
            _ui.value = FieldError(emailErr, passErr)
            return@launch
        }

        // ── 2) Lógica de negocio ─────────────────────────────────────
        _ui.value = SignInUiState.Loading
        when (val res = signInUseCase(UserCredentials(email.trim(), pass))) {
            is AuthResult.Success -> _ui.value = SignInUiState.Success(res.data)
            is AuthResult.Error   -> _ui.value = mapError(res.error)
        }
    }


    /* ──────────────── Google One-Tap ───────────────────── */
    fun finishGoogleSignIn(idToken: String) = viewModelScope.launch {
        _ui.value = Loading

        when (val res = googleUseCase(idToken)) {
            is AuthResult.Success -> _ui.value = SignInUiState.Success(true) // Google = verificado
            is AuthResult.Error   -> _ui.value = mapError(res.error)
        }
    }

    /** Reinicia el estado para descartar errores ya mostrados. */
    fun consumeError() { _ui.value = SignInUiState.Idle }

    /* ──────────────── Mapper de errores ────────────────── */
    private fun mapError(err: AuthError): SignInUiState = when (err) {
        AuthError.InvalidCredentials ->
            FieldError(emailError = "Correo o contraseña incorrectos")
        AuthError.UserNotFound ->
            FieldError(emailError = "Cuenta no registrada o deshabilitada")
        AuthError.Network, AuthError.Timeout ->
            Error("Sin conexión. Intenta nuevamente")
        AuthError.EmailNotVerified ->
            Error("Confirma tu correo antes de ingresar")
        is AuthError.Unknown ->
            Error(err.cause.message ?: "Error desconocido")

        AuthError.EmailAlreadyUsed -> TODO()
        AuthError.WeakPassword -> TODO()
    }
}
