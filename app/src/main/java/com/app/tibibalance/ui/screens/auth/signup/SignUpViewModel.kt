/* ui/screens/auth/signup/SignUpViewModel.kt */
package com.app.tibibalance.ui.screens.auth.signup

import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SendVerificationEmailUseCase
import com.app.domain.usecase.auth.SignUpUseCase
import com.app.tibibalance.ui.components.utils.mapAuthErrorToMessage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate
import javax.inject.Inject

/**
 * @file        SignUpViewModel.kt
 * @ingroup     ui_screens_auth_signup
 * @brief       ViewModel encargado del flujo de registro de nuevos usuarios (email y Google).
 *
 * @details
 *  - Consume [SignUpUseCase], [GoogleSignInUseCase] y [SendVerificationEmailUseCase], los cuales retornan [AuthResult].
 *  - Mantiene el estado de UI en un [MutableStateFlow] expuesto como [StateFlow], utilizando
 *    la interfaz selada [SignUpUiState] para representar Idle, Loading, errores de campo,
 *    errores globales y éxitos (envío de email de verificación o Google).
 *  - Traduce los [AuthError] de dominio a [SignUpUiState.Error] usando [mapAuthErrorToMessage],
 *    de modo que la capa Compose reaccione sin conocer detalles de infraestructura ni lógica de negocio.
 *
 * Ejemplos de flujo:
 *  1) Usuario llena formulario con nombre, fecha, correo y contraseñas.
 *  2) Llamada a [signUpUseCase] para crear cuenta; si hay error de dominio, se traduce a mensaje con emojis.
 *  3) Si es exitoso, se asume que el sistema envió un correo de verificación y se emite [SignUpUiState.VerificationEmailSent].
 *  4) Para registro vía Google, se llama a [googleSignInUseCase]; si falla, se traduce el [AuthError].
 *
 * @see SignUpUiState Estados posibles de la UI para esta pantalla.
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val googleUseCase: GoogleSignInUseCase,
    private val sendEmailUseCase: SendVerificationEmailUseCase
) : ViewModel() {

    /** Estado interno mutable para exponer el estado de UI (inmutable desde el exterior). */
    private val _ui = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)

    /** Estado inmutable observado por la capa Compose. */
    val ui: StateFlow<SignUpUiState> = _ui

    /* ─────── validaciones locales ─────── */

    /**
     * Valida el nombre de usuario:
     *  - No puede estar vacío.
     *  - Si tiene menos de 4 caracteres, solo permite letras, números o guion bajo.
     *  - Si tiene 4 caracteres, los mismos caracteres base.
     *  - Si tiene 5 o más, permite espacios internos además de letras, números y guion bajo.
     *
     * @param username Cadena ingresada por el usuario.
     * @return Mensaje de error si falla la validación, o null si es válido.
     */
    private fun usernameError(username: String): String? {
        val u = username.trim()
        val baseCharsRegex = Regex("^[a-zA-Z0-9_]+$")
        val extendedCharsRegex = Regex("^[a-zA-Z0-9_ ]+$")

        return when {
            u.isBlank() -> "Requerido"
            u.length < 4 -> {
                if (!u.matches(baseCharsRegex)) "Solo letras, números o _" else "≥ 4 caracteres"
            }
            u.length == 4 -> {
                if (!u.matches(baseCharsRegex)) "Solo letras, números o _" else null
            }
            else -> {
                if (!u.matches(extendedCharsRegex)) "Solo letras, números, _, o espacios internos" else null
            }
        }
    }

    /**
     * Valida la fecha de nacimiento:
     *  - No puede ser nula.
     *  - No puede ser futura.
     *  - El usuario debe tener al menos 18 años.
     *
     * @param d Fecha de nacimiento como [LocalDate] o null.
     * @return Mensaje de error si falla la validación, o null si es válido.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun birthDateError(d: LocalDate?): String? = when {
        d == null -> "Selecciona fecha"
        d.isAfter(LocalDate.now()) -> "Fecha inválida"
        LocalDate.now().minusYears(18).isBefore(d) -> "Debes tener +18"
        else -> null
    }

    /**
     * Extensión de [String] para verificar patrón de email.
     * @return true si la cadena coincide con el patrón de email, false de otro modo.
     */
    private fun String.isValidEmail() =
        Patterns.EMAIL_ADDRESS.matcher(this).matches()

    /**
     * Valida la fortaleza de la contraseña:
     *  - Mínimo 8 caracteres.
     *  - Al menos una mayúscula, una minúscula, un número y un símbolo.
     *
     * @return Mensaje con requerimientos faltantes si falla, o null si es válida.
     */
    private fun String.passwordStrengthError(): String? {
        val req = listOfNotNull(
            "• Mínimo 8"       .takeIf { length < 8 },
            "• Mayúscula"      .takeIf { none(Char::isUpperCase) },
            "• Minúscula"      .takeIf { none(Char::isLowerCase) },
            "• Número"         .takeIf { none(Char::isDigit) },
            "• Símbolo (!@#…)" .takeIf { none { "!@#$%^&*()_+-=[]{};:'\",.<>/?".contains(it) } }
        )
        return if (req.isEmpty()) null else "La contraseña debe:\n" + req.joinToString("\n")
    }

    /* ─────── API público ─────── */

    /**
     * Inicia el proceso de registro con los datos ingresados por el usuario.
     *
     * 1) Valida localmente los campos: nombre de usuario, fecha de nacimiento, email y contraseñas.
     * 2) Si hay error de validación, emite [SignUpUiState.FieldError] con los mensajes correspondientes.
     * 3) Emite [SignUpUiState.Loading] y llama a [signUpUseCase].
     * 4) Si [signUpUseCase] retorna [AuthResult.Success], envía email de verificación con [sendEmailUseCase]
     *    y emite [SignUpUiState.VerificationEmailSent].
     * 5) Si retorna [AuthResult.Error], traduce el [AuthError] a mensaje usando [mapAuthErrorToMessage]
     *    y emite [SignUpUiState.Error].
     *
     * @param userName   Nombre de usuario ingresado.
     * @param birthDate  Fecha de nacimiento como [LocalDate] opcional.
     * @param email      Correo electrónico ingresado.
     * @param password   Contraseña ingresada.
     * @param confirm    Confirmación de contraseña; debe coincidir con [password].
     * @param onSuccess  Callback opcional que se ejecuta tras éxito.
     * @param onError    Callback opcional que recibe mensaje de error tras fallo.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun signUp(
        userName: String,
        birthDate: LocalDate?,
        email: String,
        password: String,
        confirm: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // 1) Validación local
        localValidate(userName, birthDate, email, password, confirm)?.let { fieldErrorState ->
            _ui.value = fieldErrorState
            return
        }

        viewModelScope.launch {
            _ui.value = SignUpUiState.Loading

            val dobKtx = birthDate!!.toKotlinLocalDate()
            when (
                val res = signUpUseCase(
                    UserCredentials(email.trim(), password),
                    name = userName.trim(),
                    dob = dobKtx
                )
            ) {
                is AuthResult.Success -> {
                    // Envío de email de verificación antes de notificar éxito
                    sendEmailUseCase(email.trim())
                    _ui.value = SignUpUiState.VerificationEmailSent(email.trim())
                    onSuccess()
                }
                is AuthResult.Error -> {
                    // 2) Mapeamos cualquier AuthError a SignUpUiState.Error con emojis
                    val mensaje = mapAuthErrorToMessage(res.error)
                    _ui.value = SignUpUiState.Error(mensaje)
                    onError(mensaje)
                }
            }
        }
    }

    /**
     * Completa el flujo de registro a través de Google One-Tap.
     *
     * 1) Emite [SignUpUiState.Loading] mientras espera la respuesta de [googleUseCase].
     * 2) Si es [AuthResult.Success], emite [SignUpUiState.GoogleSuccess].
     * 3) Si es [AuthResult.Error], traduce el [AuthError] a mensaje usando [mapAuthErrorToMessage]
     *    y emite [SignUpUiState.Error].
     *
     * @param idToken Token JWT proporcionado por el flujo One-Tap de Google.
     */
    fun finishGoogleSignUp(idToken: String) = viewModelScope.launch {
        _ui.value = SignUpUiState.Loading

        when (val r = googleUseCase(idToken)) {
            is AuthResult.Success -> _ui.value = SignUpUiState.GoogleSuccess
            is AuthResult.Error   -> {
                val mensaje = mapAuthErrorToMessage(r.error)
                _ui.value = SignUpUiState.Error(mensaje)
            }
        }
    }

    /** Reinicia el estado de los errores de campo a Idle. */
    fun consumeFieldError() {
        if (_ui.value is SignUpUiState.FieldError) {
            _ui.value = SignUpUiState.Idle
        }
    }

    /** Reinicia el estado de error global a Idle. */
    fun consumeError() {
        if (_ui.value is SignUpUiState.Error) {
            _ui.value = SignUpUiState.Idle
        }
    }

    /** Reinicia el estado de éxito a Idle. */
    fun dismissSuccess() {
        if (_ui.value is SignUpUiState.Success || _ui.value is SignUpUiState.GoogleSuccess) {
            _ui.value = SignUpUiState.Idle
        }
    }

    /* ─────── helpers Google One-Tap ─────── */

    /**
     * Construye un [GetCredentialRequest] para Google One-Tap.
     *
     * @param clientId Client ID de OAuth2 para Google One-Tap.
     * @return Instancia de [GetCredentialRequest].
     */
    fun buildGoogleRequest(clientId: String): GetCredentialRequest =
        GetCredentialRequest(
            listOf(
                GetGoogleIdOption.Builder()
                    .setServerClientId(clientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
        )

    /* ─────── validación y mapeo de errores ─────── */

    /**
     * Valida todos los campos de registro y retorna [SignUpUiState.FieldError] si alguno falla.
     *
     * @param u  Nombre de usuario.
     * @param b  Fecha de nacimiento como [LocalDate] optional.
     * @param e  Correo electrónico.
     * @param p1 Contraseña.
     * @param p2 Confirmación de contraseña.
     * @return Estado de error de campo o null si todos son válidos.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun localValidate(
        u: String,
        b: LocalDate?,
        e: String,
        p1: String,
        p2: String
    ): SignUpUiState.FieldError? {
        val fe = SignUpUiState.FieldError(
            userNameError  = usernameError(u),
            birthDateError = birthDateError(b),
            emailError     = when {
                e.isBlank() -> "Requerido"
                !e.isValidEmail() -> "Correo mal formado"
                else -> null
            },
            pass1Error = p1.passwordStrengthError(),
            pass2Error = if (p1 != p2) "No coinciden" else null
        )
        return if (
            fe.userNameError != null ||
            fe.birthDateError != null ||
            fe.emailError != null ||
            fe.pass1Error != null ||
            fe.pass2Error != null
        ) fe else null
    }

    /**
     * Convierte un [AuthError] de dominio a [SignUpUiState.Error] usando emojis.
     *
     * @param e Error de autenticación.
     * @return Estado [SignUpUiState.Error] con mensaje apropiado.
     */
    private fun mapError(e: AuthError): SignUpUiState =
        SignUpUiState.Error(mapAuthErrorToMessage(e))
}
