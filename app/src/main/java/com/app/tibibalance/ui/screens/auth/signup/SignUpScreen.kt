/* ui/screens/auth/signup/SignUpScreen.kt */
package com.app.tibibalance.ui.screens.auth.signup

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.GoogleSignButton
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputDate
import com.app.tibibalance.ui.components.inputs.InputEmail
import com.app.tibibalance.ui.components.inputs.InputPassword
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.texts.Caption
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

private const val WEB_CLIENT_ID =
    "467927540157-tvu0re0msga2o01tsj9t1r1o6kqvek3j.apps.googleusercontent.com"

/**
 * @file    SignUpScreen.kt
 * @ingroup ui_screens_auth_signup
 * @brief   Composable para la pantalla de registro de nuevos usuarios.
 *
 * @details
 * - Recibe entrada de nombre de usuario, fecha de nacimiento, correo y contraseñas.
 * - Permite registro vía Google One-Tap.
 * - Observa [SignUpViewModel] para estados de UI ([SignUpUiState]).
 * - Muestra:
 *   • Validaciones de campo en línea dentro de [FormContainer].
 *   • [ModalInfoDialog] para Loading, Error y Éxito (verificación de correo).
 *   • [Snackbar] para mensajes globales.
 *   • Navegación reactiva según el estado (éxito o GoogleSuccess).
 *
 * @see SignUpViewModel ViewModel que gestiona la lógica de negocio.
 * @see SignUpUiState  Estados posibles de la UI para esta pantalla.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpScreen(
    nav: NavController,
    vm: SignUpViewModel = hiltViewModel()
) {
    /* ----- estado local ----- */
    var username by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    var email by remember { mutableStateOf("") }
    var pass1 by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    /* ----- captura de estado global ----- */
    val uiState by vm.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /* ----- Credential-Manager y helpers ----- */
    val ctx = LocalContext.current
    val activity = ctx as Activity
    val cm = remember(activity) { CredentialManager.create(activity) }

    fun launchGoogle() = scope.launch {
        try {
            val res = cm.getCredential(activity, vm.buildGoogleRequest(WEB_CLIENT_ID))
            val token = GoogleIdTokenCredential.createFrom(res.credential.data).idToken
            if (token.isNullOrBlank()) {
                snackbar.showSnackbar("Token vacío")
            } else {
                vm.finishGoogleSignUp(token)
            }
        } catch (e: Exception) {
            snackbar.showSnackbar(e.message ?: "Google cancelado")
        }
    }

    /* ----- navegación reactiva según uiState ----- */
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SignUpUiState.VerificationEmailSent -> {
                nav.navigate(Screen.VerifyEmail.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                }
                vm.dismissSuccess()
            }
            is SignUpUiState.GoogleSuccess -> {
                nav.navigate(Screen.Main.route) {
                    popUpTo(Screen.Launch.route) { inclusive = true }
                }
            }
            is SignUpUiState.Error -> {
                // Mostrar Snackbar con mensaje de error (con emojis)
                scope.launch { snackbar.showSnackbar(state.message) }
                vm.consumeError()
            }
            else -> Unit
        }
    }

    /* ----- flags de diálogo ----- */
    val loading = uiState is SignUpUiState.Loading
    val fieldErr = uiState as? SignUpUiState.FieldError
    val isError = uiState is SignUpUiState.Error
    val isSuccess = uiState is SignUpUiState.VerificationEmailSent

    ModalInfoDialog(
        visible = loading || isError || isSuccess,
        loading = loading,
        icon = when {
            isSuccess -> Icons.Default.Check
            isError -> Icons.Default.Error
            else -> null
        },
        iconColor = when {
            isSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
            isError -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onPrimary
        },
        iconBgColor = when {
            isSuccess -> MaterialTheme.colorScheme.primaryContainer
            isError -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        title = when {
            isSuccess -> "Cuenta creada"
            isError -> "Error"
            else -> null
        },
        message = when {
            isSuccess -> "Te enviamos un enlace para verificar tu correo."
            isError -> (uiState as SignUpUiState.Error).message
            else -> null
        },
        primaryButton = when {
            isSuccess -> DialogButton("Continuar") {
                vm.dismissSuccess()
                nav.navigate(Screen.VerifyEmail.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                }
            }
            isError -> DialogButton("Aceptar") {
                vm.consumeError()
            }
            else -> null
        },
        dismissOnBack = !loading,
        dismissOnClickOutside = !loading
    )

    /* ----- UI principal ----- */
    val gradient = gradient()

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient)
    ) {
        /* Scrollable column con inputs y botones */
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 80.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageContainer(
                resId = R.drawable.img_signup,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(bottom = 24.dp)
            )

            FormContainer {
                InputText(
                    value = username,
                    onValueChange = { username = it; vm.consumeFieldError() },
                    placeholder = "Nombre de usuario*",
                    isError = fieldErr?.userNameError != null,
                    supportingText = fieldErr?.userNameError
                )

                val fmt = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
                InputDate(
                    value = birthDate?.format(fmt) ?: "",
                    onClick = {
                        val c = Calendar.getInstance()
                        DatePickerDialog(
                            ctx,
                            { _, y, m, d ->
                                birthDate = LocalDate.of(y, m + 1, d)
                                vm.consumeFieldError()
                            },
                            c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH]
                        ).show()
                    },
                    isError = fieldErr?.birthDateError != null,
                    supportingText = fieldErr?.birthDateError
                )

                InputEmail(
                    value = email,
                    onValueChange = { email = it; vm.consumeFieldError() },
                    label = "Correo*",
                    isError = fieldErr?.emailError != null,
                    supportingText = fieldErr?.emailError
                )

                InputPassword(
                    value = pass1,
                    onValueChange = { pass1 = it; vm.consumeFieldError() },
                    label = "Contraseña*",
                    isError = fieldErr?.pass1Error != null,
                    supportingText = fieldErr?.pass1Error
                )

                InputPassword(
                    value = pass2,
                    onValueChange = { pass2 = it; vm.consumeFieldError() },
                    label = "Confirmar contraseña*",
                    isError = fieldErr?.pass2Error != null,
                    supportingText = fieldErr?.pass2Error
                )
            }

            Spacer(Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(R.string.btn_sign_up),
                enabled = !loading,
                onClick = {
                    vm.signUp(
                        userName = username,
                        birthDate = birthDate,
                        email = email,
                        password = pass1,
                        confirm = pass2
                        // Se remueven onSuccess y onError:
                        // el LaunchedEffect(uiState) manejará navegación y errores.
                    )

                    // Intento de guardar credenciales localmente
                    scope.launch {
                        try {
                            cm.createCredential(
                                activity,
                                CreatePasswordRequest(username, pass1)
                            )
                        } catch (_: Exception) {
                        }
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text("  •  ")
                HorizontalDivider(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            GoogleSignButton(onClick = ::launchGoogle)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Caption("¿Ya tienes cuenta? ")
                TextButtonLink(
                    text = stringResource(R.string.btn_sign_in),
                    onClick = { nav.navigate(Screen.SignIn.route) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        Header(
            title = stringResource(R.string.sign_up_title),
            showBackButton = true,
            onBackClick = { nav.navigateUp() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
