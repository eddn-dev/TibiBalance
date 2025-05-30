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
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.theme.AccountSettings
import com.app.tibibalance.ui.theme.BluePrimaryLight
import com.app.tibibalance.ui.theme.LegalSettings
import com.app.tibibalance.ui.theme.PreferencesSettings
import com.app.tibibalance.ui.theme.gradient

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

    when {
        ui.loading       -> Centered("Cargando…")
        ui.error != null -> Centered(ui.error!!)
        ui.user != null  -> SettingsContent(
            user                 = ui.user!!,
            navController        = navController,
            signingOut           = ui.signingOut,
            /* VM callbacks */
            onChangeTheme        = vm::changeTheme,
            onToggleGlobalNotif  = vm::toggleGlobalNotif,
            onToggleTTS          = vm::toggleTTS,
            onSignOut            = vm::signOut
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
    onSignOut           : () -> Unit
) {
    /* Destinos secundarios */
    val onEditPersonal   = { navController.navigate(Screen.EditProfile.route) }
    val onConfigureNotis = { navController.navigate(Screen.ConfigureNotif.route) }

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        SettingsBody(
            user                 = user,
            onEditPersonal       = onEditPersonal,
            onDevices            = { /* TODO */ },
            onAchievements       = { /* TODO */ },
            onConfigureNotis     = onConfigureNotis,
            onChangeTheme        = onChangeTheme,
            onToggleGlobalNotif  = onToggleGlobalNotif,
            onToggleTTS          = onToggleTTS,
            onSignOut            = onSignOut,
            signingOut           = signingOut,
            onDeleteAccount      = { /* TODO */ },
            onOpenTerms          = { /* TODO */ },
            onOpenPrivacy        = { /* TODO */ }
        )
    }
}

/* ───────────────────────  Cuerpo scrollable ─────────────────── */

@Composable
private fun SettingsBody(
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
    onDeleteAccount    : () -> Unit
) {
    /* diálogos */
    val snackbar = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title           = { Text("Eliminar cuenta") },
            text            = { Text("¿Seguro? Esta acción es irreversible.") },
            confirmButton   = {
                TextButton(
                    onClick = { onDeleteAccount(); showDeleteDialog = false }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton   = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
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
            contentDescription = "Configuracion",
            modifier = Modifier.size(128.dp)
        )
        Title(
            text = "Configuración",
        )
        /* ── Grupo: Cuenta ── */
        FormContainer(backgroundColor = AccountSettings) {
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
        FormContainer(backgroundColor = PreferencesSettings) {
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
        FormContainer(backgroundColor = LegalSettings) {
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
