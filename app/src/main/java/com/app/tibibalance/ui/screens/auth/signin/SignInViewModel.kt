// :app/ui/screens/auth/signin/SignInViewModel.kt
package com.app.tibibalance.ui.screens.auth.signin

import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.UserCredentials
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.FirebaseNetworkException

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase   : SignInUseCase,
    private val googleUseCase   : GoogleSignInUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val ui: StateFlow<SignInUiState> = _ui

    fun signIn(email: String, pass: String) = viewModelScope.launch {
        /* validación … */
        _ui.value = SignInUiState.Loading
        signInUseCase(UserCredentials(email.trim(), pass))
            .onSuccess { verified -> _ui.value = SignInUiState.Success(verified) }
            .onFailure {  _ui.value = mapError(it) }
    }

    fun finishGoogleSignIn(idToken: String) = viewModelScope.launch {
        _ui.value = SignInUiState.Loading
        googleUseCase(idToken)
            .onSuccess { _ui.value = SignInUiState.Success(verified = true) }
            .onFailure { _ui.value = SignInUiState.Error(it.message ?: "Google error") }
    }

    public fun consumeError() {
        _ui.value = SignInUiState.Idle
    }


    private fun mapError(e: Throwable): SignInUiState = when (e) {
        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
            SignInUiState.FieldError(emailError = "Cuenta no registrada o deshabilitada")
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
            SignInUiState.FieldError(emailError = "Correo o contraseña incorrectos")
        is FirebaseNetworkException ->
            SignInUiState.Error("Sin conexión. Intenta nuevamente")
        else -> SignInUiState.Error(e.message ?: "Error desconocido")
    }
}
