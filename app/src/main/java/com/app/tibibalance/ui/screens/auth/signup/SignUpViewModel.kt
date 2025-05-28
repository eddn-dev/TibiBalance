package com.app.tibibalance.ui.screens.auth.signup

import android.os.Build
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SignUpUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import android.util.Patterns                                         // :contentReference[oaicite:0]{index=0}
import androidx.annotation.RequiresApi
import com.app.domain.usecase.auth.SendVerificationEmailUseCase
import kotlinx.datetime.toKotlinLocalDate

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase : SignUpUseCase,
    private val googleUseCase : GoogleSignInUseCase,
    private val sendEmailUseCase: SendVerificationEmailUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val ui: StateFlow<SignUpUiState> = _ui             // read-only

    /* ─────── validaciones locales ─────── */
    private fun usernameError(username: String): String? {
        // 1. Truncar espacios al inicio y al fin de la cadena ingresada.
        val u = username.trim()

        // Regex para caracteres base permitidos (sin espacios).
        val baseCharsRegex = Regex("^[a-zA-Z0-9_]+$")

        // Regex para caracteres permitidos cuando la longitud es > 4 (incluye espacios).
        val extendedCharsRegex = Regex("^[a-zA-Z0-9_ ]+$")

        return when {
            // 2. Validación de cadena vacía (después del trim).
            u.isBlank() -> "Requerido"

            // 3. Validación de longitud mínima y caracteres para longitudes < 4.
            u.length < 4 -> {
                if (!u.matches(baseCharsRegex)) {
                    "Solo letras, números o _"
                } else {
                    "≥ 4 caracteres"
                }
            }

            // 4. Validación para longitud == 4.
            u.length == 4 -> {
                if (!u.matches(baseCharsRegex)) {
                    "Solo letras, números o _"
                } else {
                    null // Válido
                }
            }

            // 5. Validación para longitud > 4 (es decir, u.length >= 5).
            else -> { // u.length > 4
                if (!u.matches(extendedCharsRegex)) {
                    "Solo letras, números, _, o espacios internos"
                } else {
                    null // Válido
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun birthDateError(d: LocalDate?) = when {
        d == null                  -> "Selecciona fecha"
        d.isAfter(LocalDate.now()) -> "Fecha inválida"
        LocalDate.now().minusYears(18).isBefore(d) -> "Debes tener +18"
        else                       -> null
    }

    private fun String.isValidEmail() =
        Patterns.EMAIL_ADDRESS.matcher(this).matches()             // :contentReference[oaicite:1]{index=1}

    private fun String.passwordStrengthError(): String? {
        val req = listOfNotNull(
            "• Mínimo 8"        .takeIf { length < 8 },
            "• Mayúscula"       .takeIf { none(Char::isUpperCase) },
            "• Minúscula"       .takeIf { none(Char::isLowerCase) },
            "• Número"          .takeIf { none(Char::isDigit) },
            "• Símbolo (!@#…)"  .takeIf { none { "!@#$%^&*()_+-=[]{};:'\",.<>/?".contains(it) } }
        )
        return if (req.isEmpty()) null else "La contraseña debe:\n" + req.joinToString("\n")
    }

    /* ─────── API público ─────── */
    @RequiresApi(Build.VERSION_CODES.O)
    fun signUp(
        userName : String,
        birthDate: LocalDate?,
        email    : String,
        password : String,
        confirm  : String,
        onSuccess: () -> Unit = {},
        onError  : (String) -> Unit = {}
    ) {
        // Validación local
        localValidate(userName, birthDate, email, password, confirm)?.let {
            _ui.value = it; return
        }

        viewModelScope.launch {
            _ui.value = SignUpUiState.Loading

            val dobKtx = birthDate!!.toKotlinLocalDate()
            when (
                val res = signUpUseCase(
                    UserCredentials(email.trim(), password),
                    name = userName,
                    dob = dobKtx
                )
            ) {
                is AuthResult.Success -> {
                    val emailSent = sendEmailUseCase(email)
                    _ui.value = SignUpUiState.VerificationEmailSent(email)
                    onSuccess()
                }

                is AuthResult.Error -> {
                    _ui.value = mapError(res.error)
                    onError("Falló el registro: ${res.error}")
                }
            }
        }
    }

    fun finishGoogleSignUp(idToken: String) = viewModelScope.launch {
        _ui.value = SignUpUiState.Loading
        when (val r = googleUseCase(idToken)) {
            is AuthResult.Success -> _ui.value = SignUpUiState.GoogleSuccess
            is AuthResult.Error   -> _ui.value = mapError(r.error)
        }
    }

    fun consumeFieldError() { if (_ui.value is SignUpUiState.FieldError) _ui.value = SignUpUiState.Idle }
    fun consumeError()      { if (_ui.value is SignUpUiState.Error)      _ui.value = SignUpUiState.Idle }
    fun dismissSuccess()    { if (_ui.value is SignUpUiState.Success ||
        _ui.value is SignUpUiState.GoogleSuccess) _ui.value = SignUpUiState.Idle }

    /* ─────── helpers Google One-Tap ─────── */
    fun buildGoogleRequest(clientId: String): GetCredentialRequest =
        GetCredentialRequest(
            listOf(
                GetGoogleIdOption.Builder()
                    .setServerClientId(clientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()                                         // :contentReference[oaicite:2]{index=2}
            )
        )

    /* ─────── mappers ─────── */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun localValidate(
        u: String, b: LocalDate?, e: String, p1: String, p2: String
    ): SignUpUiState.FieldError? {
        val fe = SignUpUiState.FieldError(
            userNameError  = usernameError(u),
            birthDateError = birthDateError(b),
            emailError     = when {
                e.isBlank()        -> "Requerido"
                !e.isValidEmail()  -> "Correo mal formado"
                else               -> null
            },
            pass1Error = p1.passwordStrengthError(),
            pass2Error = if (p1 != p2) "No coinciden" else null
        )
        return if (listOf(
                fe.userNameError, fe.birthDateError, fe.emailError,
                fe.pass1Error, fe.pass2Error).any { it != null }) fe else null
    }

    private fun mapError(e: AuthError): SignUpUiState = when (e) {
        AuthError.EmailAlreadyUsed ->
            SignUpUiState.FieldError(emailError = "Correo ya registrado")     // :contentReference[oaicite:3]{index=3}
        AuthError.WeakPassword     ->
            SignUpUiState.FieldError(pass1Error = "Contraseña muy débil")     // :contentReference[oaicite:4]{index=4}
        AuthError.Network,
        AuthError.Timeout          ->
            SignUpUiState.Error("Sin conexión. Intenta más tarde.")
        else                       ->
            SignUpUiState.Error("Error: ${e::class.simpleName}")
    }
}
