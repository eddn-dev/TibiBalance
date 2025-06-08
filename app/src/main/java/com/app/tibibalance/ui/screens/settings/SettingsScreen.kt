/* ui/screens/settings/SettingsScreen.kt */
package com.app.tibibalance.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.domain.entities.User
import com.app.domain.enums.ThemeMode
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.DangerButton
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.ConfirmDeleteDialog
import com.app.tibibalance.ui.components.dialogs.DeleteAccountDialog
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen
import android.app.Activity
import androidx.credentials.CredentialManager
import com.app.tibibalance.auth.GoogleOneTapHelper
import kotlinx.coroutines.launch
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.util.Base64
import androidx.compose.material.icons.filled.Error
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.screens.auth.signin.SignInUiState
import org.json.JSONObject

private const val WEB_CLIENT_ID =
    "467927540157-tvu0re0msga2o01tsj9t1r1o6kqvek3j.apps.googleusercontent.com"

private suspend fun requestGoogleIdToken(
    activity: Activity,
    cm: CredentialManager
): String {
    // Solo mostrar la cuenta previamente autenticada
    val request = GoogleOneTapHelper.buildRequest(
        serverClientId = WEB_CLIENT_ID,
        authorizedOnly = true,
    )
    val result = cm.getCredential(activity, request)
    return GoogleIdTokenCredential.createFrom(result.credential.data).idToken
}


/* ─────────────────────────  Entry  ─────────────────────────── */

@Composable
fun SettingsScreen(
    navController: NavHostController
) {
    val vm: SettingsViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()

    /* Navegación a Launch cuando se cierre sesión */
    LaunchedEffect(Unit) {
        vm.loggedOut.collect {
            navController.navigate(Screen.Launch.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(ui.navigatingToGoodbye) {
        if (ui.navigatingToGoodbye) {
            navController.navigate(Screen.Goodbye.route) {
                popUpTo(Screen.Settings.route) { inclusive = true }
            }
        }
    }

    when {
        ui.loading       -> Centered("Cargando…")
        ui.user != null  -> SettingsContent(
            user = ui.user!!,
            navController = navController,
            signingOut = ui.signingOut,
            onChangeTheme = vm::changeTheme,
            onToggleGlobalNotif = vm::toggleGlobalNotif,
            onToggleTTS = vm::toggleTTS,
            onSignOut = vm::signOut,
            vm = vm,
            ui = ui
        )
    }
}

/* ────────────────────  Contenedor top-level  ─────────────────── */

@Composable
private fun SettingsContent(
    user                : User,
    navController       : NavHostController,
    signingOut          : Boolean,
    /* VM callbacks */
    onChangeTheme       : (ThemeMode) -> Unit,
    onToggleGlobalNotif : (Boolean) -> Unit,
    onToggleTTS         : (Boolean) -> Unit,
    onSignOut           : () -> Unit,
    vm: SettingsViewModel,
    ui: SettingsViewModel.UiState
) {
    /* Destinos secundarios */
    val onEditPersonal   = { navController.navigate(Screen.EditProfile.route) }
    val onConfigureNotis = { navController.navigate(Screen.ConfigureNotif.route) }
    val vm: SettingsViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient())
    ) {

        SettingsBody(
            ui                   = ui,
            vm                   = vm,
            user                 = user,
            onEditPersonal       = onEditPersonal,
            onDevices            = { /* TODO */ },
            onAchievements       = { navController.navigate(Screen.Achievements.route) },
            onConfigureNotis     = onConfigureNotis,
            onChangeTheme        = onChangeTheme,
            onToggleGlobalNotif  = onToggleGlobalNotif,
            onToggleTTS          = onToggleTTS,
            onSignOut            = onSignOut,
            signingOut           = signingOut,
            onDeleteAccount      = vm::reauthenticateAndDelete,
            onOpenTerms          = { /* TODO */ },
            onOpenPrivacy        = { /* TODO */ }
        )
    }
}

/* ───────────────────────  Cuerpo scrollable ─────────────────── */

@Composable
private fun SettingsBody(
    ui                 : SettingsViewModel.UiState,
    vm                 : SettingsViewModel,
    user               : User,
    /* Cuenta */
    onEditPersonal     : () -> Unit,
    onDevices          : () -> Unit,
    onAchievements     : () -> Unit,
    onConfigureNotis   : () -> Unit,
    /* Preferencias */
    onChangeTheme      : (ThemeMode) -> Unit,
    onToggleGlobalNotif: (Boolean) -> Unit,
    onToggleTTS        : (Boolean) -> Unit,
    onOpenTerms        : () -> Unit,
    onOpenPrivacy      : () -> Unit,
    /* Sesión */
    onSignOut          : () -> Unit,
    signingOut         : Boolean,
    onDeleteAccount    : (password: String?, googleIdToken: String?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as Activity
    val cm = remember(activity) { CredentialManager.create(activity) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val errorMessage = ui.error
    val tag = "DeleteAccount"

    var showGooglePreConfirmDialog by remember { mutableStateOf(false) }
    var showWrongGoogleAccountDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            visible = showDeleteDialog,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                // Detecta el proveedor del usuario
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val providerId = firebaseUser?.providerData?.getOrNull(1)?.providerId

                when (providerId) {
                    "password" -> {
                        showDeleteDialog = false
                        showPasswordDialog = true
                    }
                    "google.com" -> {
                        showDeleteDialog = false
                        showGooglePreConfirmDialog = true
                    }
                    else -> {
                        vm.clearError()
                        showErrorDialog = true
                        showDeleteDialog = false
                    }
                }
            }
        )
    }

    if (showGooglePreConfirmDialog) {
        ModalInfoDialog(
            visible = showGooglePreConfirmDialog,
            icon    = Icons.Default.Person,
            title   = "Confirma tu cuenta de Google",
            message = "A continuación se abrirá Google One Tap.\n\nAsegúrate de elegir la misma cuenta con la que iniciaste sesión.",
            primaryButton = DialogButton("Continuar") {
                showGooglePreConfirmDialog = false
                scope.launch {
                    try {
                        val token = requestGoogleIdToken(activity, cm)

                        val currentUid = FirebaseAuth.getInstance()
                            .currentUser?.providerData
                            ?.find { it.providerId == "google.com" }?.uid

                        val tokenInfo = GoogleOneTapHelper.decodeToken(token)
                        val selectedUid = tokenInfo["sub"] as? String

                        if (selectedUid != currentUid) {
                            showWrongGoogleAccountDialog = true
                            return@launch
                        }

                        Log.d(tag, "LAYTON Token OK: $token")
                        onDeleteAccount(null, token)
                    } catch (e: Exception) {
                        vm.setError(e.message ?: "Error durante autenticación")
                        showErrorDialog = true
                    }
                }
            },
            secondaryButton = DialogButton("Cancelar") {
                showGooglePreConfirmDialog = false
            }
        )
    }
    /*if (showGooglePreConfirmDialog) {
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
}*/
    if (showWrongGoogleAccountDialog) {
        ModalInfoDialog(
            visible = true,
            icon    = Icons.Default.Error,
            title   = "Cuenta incorrecta",
            message = "La cuenta seleccionada no coincide con la que usaste para iniciar sesión. Intenta de nuevo.",
            primaryButton = DialogButton("Aceptar") {
                showWrongGoogleAccountDialog = false
            }
        )
    }


    if (showPasswordDialog) {
        DeleteAccountDialog(
            visible = showPasswordDialog,
            onDismiss = { showPasswordDialog = false },
            onConfirm = { password ->
                onDeleteAccount(password, null)
                showPasswordDialog = false
            }
        )
    }

    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error al eliminar cuenta") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    vm.clearError()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    /* estado local para switches/botones – sincronizado con el VM */
    val settings = user.settings
    var theme     by remember { mutableStateOf(settings.theme) }
    var notifGlob by remember { mutableStateOf(settings.notifGlobal) }
    var tts       by remember { mutableStateOf(settings.accessibilityTTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 96.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ImageContainer(
            resId = R.drawable.ic_settings,
            contentDescription = "Configuración",
            modifier = Modifier.size(128.dp)
        )
        Title(text = "Configuración")

        /* ── Grupo: Cuenta ── */
        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
            SettingItem(
                leadingIcon = { Icon24(Icons.AutoMirrored.Filled.ListAlt) },
                text        = "Editar información personal",
                onClick     = onEditPersonal
            )
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.Person) },
                text        = "Administrar dispositivos",
                onClick     = onDevices
            )
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.StarOutline) },
                text        = "Logros y rachas",
                onClick     = onAchievements
            )
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.NotificationsNone) },
                text        = "Configurar notificaciones",
                onClick     = onConfigureNotis
            )
        }

        /* ── Grupo: Preferencias ── */
        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.Palette) },
                text        = "Tema: ${theme.label()}",
                onClick     = {
                    theme = theme.next().also(onChangeTheme)
                }
            )
            SwitchSettingItem(
                leadingIcon      = { Icon24(Icons.Default.NotificationsActive) },
                text             = "Notificaciones globales",
                checked          = notifGlob,
                onCheckedChange  = {
                    notifGlob = it
                    onToggleGlobalNotif(it)
                }
            )
            SwitchSettingItem(
                leadingIcon      = { Icon24(Icons.Default.RecordVoiceOver) },
                text             = "Texto a voz (TTS)",
                checked          = tts,
                onCheckedChange  = {
                    tts = it
                    onToggleTTS(it)
                }
            )
        }

        /* ── Grupo: Legal ── */
        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.Description) },
                text        = "Términos de uso",
                onClick     = onOpenTerms
            )
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.PrivacyTip) },
                text        = "Política de privacidad",
                onClick     = onOpenPrivacy
            )
        }

        /* ── Sesión ── */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DangerButton(
                text     = if (signingOut) "Cerrando…" else "Cerrar sesión",
                onClick  = onSignOut,
                enabled  = !signingOut,
                modifier = Modifier.weight(1f)
            )
            DangerButton(
                text     = "Eliminar cuenta",
                onClick  = { showDeleteDialog = true },
                modifier = Modifier.weight(1f)
            )
        }
    }

    SnackbarHost(snackbar)
}
fun GoogleOneTapHelper.decodeToken(token: String): Map<String, Any> {
    val parts = token.split(".")
    if (parts.size < 2) throw IllegalArgumentException("Token JWT inválido")
    val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
    return JSONObject(payload).let { json ->
        json.keys().asSequence().associateWith { json[it] }
    }
}

/* ───────────────────── Helpers ───────────────────── */

@Composable
private fun Icon24(icon: ImageVector) =
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary
        , modifier = Modifier.size(24.dp))

@Composable
private fun SwitchSettingItem(
    leadingIcon     : @Composable () -> Unit,
    text            : String,
    checked         : Boolean,
    onCheckedChange : (Boolean) -> Unit
) = SettingItem(
    leadingIcon = leadingIcon,
    text        = text,
    trailing    = { SwitchToggle(checked = checked, onCheckedChange = onCheckedChange) }
)

/* extensiones ThemeMode */
private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "Sistema"
    ThemeMode.LIGHT  -> "Claro"
    ThemeMode.DARK   -> "Oscuro"
}
private fun ThemeMode.next() = when (this) {
    ThemeMode.SYSTEM -> ThemeMode.LIGHT
    ThemeMode.LIGHT  -> ThemeMode.DARK
    ThemeMode.DARK   -> ThemeMode.SYSTEM
}

@Composable
private fun Centered(msg: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(msg)
    }
