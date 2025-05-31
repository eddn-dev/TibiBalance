// ui/screens/auth/signin/SignInScreen.kt
package com.app.tibibalance.ui.screens.auth.signin

import android.app.Activity
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.tibibalance.R
import com.app.tibibalance.auth.GoogleOneTapHelper
import com.app.tibibalance.ui.components.buttons.GoogleSignButton
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputEmail
import com.app.tibibalance.ui.components.inputs.InputPassword
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.theme.*
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private const val WEB_CLIENT_ID =
    "467927540157-tvu0re0msga2o01tsj9t1r1o6kqvek3j.apps.googleusercontent.com"

@Composable
fun SignInScreen(
    nav: NavController,
    vm : SignInViewModel = hiltViewModel()
) {
    /* --- estado de inputs --- */
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    /* --- estado global --- */
    val uiState  by vm.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope    = rememberCoroutineScope()

    /* --- Credential-Manager --- */
    val ctx      = LocalContext.current
    val activity = ctx as Activity
    val cm       = remember(activity) { CredentialManager.create(activity) }

    /* --- navegación reactivamente --- */
    /* dentro de SignInScreen.kt – LaunchedEffect(uiState) */

    LaunchedEffect(uiState) {
        (uiState as? SignInUiState.Success)?.let { s ->

            // 1) si NO está verificado → VerifyEmail  (igual que antes)
            if (!s.verified) {
                nav.navigate(Screen.VerifyEmail.route) {
                    popUpTo(Screen.Launch.route) { inclusive = true }
                }
                return@LaunchedEffect
            }

            // 2) verificado → deja que LaunchScreen decida (onboarding vs main)
            nav.navigate(Screen.Launch.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
            }
        }
    }


    /* --- Google One-Tap launcher --- */
    fun launchGoogleSignIn() = scope.launch {
        val req = GoogleOneTapHelper.buildRequest(WEB_CLIENT_ID)
        try {
            val resp   = cm.getCredential(activity, req)
            val idTkn  = GoogleIdTokenCredential.createFrom(resp.credential.data).idToken
            if (idTkn.isBlank()) { snackbar.showSnackbar("Token vacío"); return@launch }
            vm.finishGoogleSignIn(idTkn)
        } catch (ex: Exception) {
            snackbar.showSnackbar("Google cancelado: ${ex.message}")
        }
    }

    /* --- diálogo global --- */
    val showDialog = uiState is SignInUiState.Loading || uiState is SignInUiState.Error
    ModalInfoDialog(
        visible = showDialog,
        loading = uiState is SignInUiState.Loading,
        icon    = if (uiState is SignInUiState.Error) Icons.Default.Error else null,
        title   = if (uiState is SignInUiState.Error) "Error" else null,
        message = (uiState as? SignInUiState.Error)?.message,
        primaryButton = if (uiState is SignInUiState.Error)
            DialogButton("Aceptar") { vm.consumeError() } else null,
        dismissOnBack = uiState !is SignInUiState.Loading,
        dismissOnClickOutside = uiState !is SignInUiState.Loading
    )

    val fieldErr = uiState as? SignInUiState.FieldError

    /* --- UI principal --- */
    /* val gradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.primary.copy(.25f), MaterialTheme.colorScheme.background)
    ) */

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient())
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 80.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageContainer(
                resId = R.drawable.img_login,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(Modifier.height(24.dp))

            FormContainer {
                InputEmail(
                    value = email,
                    onValueChange = { email = it; vm.consumeError() },
                    isError = fieldErr?.emailError != null,
                    supportingText = fieldErr?.emailError
                )
                InputPassword(
                    value = pass,
                    onValueChange = { pass = it; vm.consumeError() },
                    isError = fieldErr?.passError != null,
                    supportingText = fieldErr?.passError
                )
            }

            Spacer(Modifier.height(12.dp))
            TextButtonLink(
                text = "¿Olvidaste tu contraseña?", onClick = { nav.navigate(Screen.Forgot.route) })
            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text    = stringResource(R.string.btn_sign_in),
                enabled = uiState !is SignInUiState.Loading,
                onClick = { vm.signIn(email, pass) }
            )

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f)); Text("  •  "); HorizontalDivider(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))

            GoogleSignButton(onClick = ::launchGoogleSignIn)

            Spacer(Modifier.height(24.dp))
            Row {
                Text("¿Aún no tienes cuenta? ", color = Color.Black)
                TextButtonLink(text = "Regístrate", onClick = { nav.navigate(Screen.SignUp.route) })
            }
            Spacer(Modifier.height(24.dp))
        }

        Header(
            title = "Iniciar sesión",
            showBackButton = true,
            onBackClick = { nav.navigateUp() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
