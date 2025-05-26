/* ui/screens/settings/SettingsScreen.kt */
package com.app.tibibalance.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.navigation.Screen

/* ─────────────────────────  Entry  ─────────────────────────── */

@Composable
fun SettingsScreen(
    navController: NavHostController,
    onNavigateUp: () -> Unit
) {
    /* VM y estado */
    val vm: SettingsViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) {
        vm.loggedOut.collect {
            navController.navigate(Screen.Launch.route) {
                popUpTo(Screen.Launch.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }


    when {
        ui.loading -> Centered("Cargando…")

        ui.error != null -> Centered(ui.error!!)

        ui.user != null -> SettingsContent(
            user            = ui.user!!,
            navController   = navController,
            onNavigateUp    = onNavigateUp,
            signingOut      = ui.signingOut,
            onSignOut       = vm::signOut
        )
    }
}

/* ───────────────────── Pantalla principal ──────────────────── */

@Composable
private fun SettingsContent(
    user          : User,
    navController : NavHostController,
    onNavigateUp  : () -> Unit,
    signingOut    : Boolean,
    /* acciones de alto nivel */
    onSignOut     : () -> Unit,
) {
    /* helpers de navegación pre-rellenados */
    val onEditPersonal   = { navController.navigate(Screen.EditProfile.route) }
    val onConfigureNotis = { navController.navigate(Screen.ConfigureNotif.route) }

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        SettingsBody(
            user               = user,
            onEditPersonal     = onEditPersonal,
            onDevices          = { /* TODO */ },
            onAchievements     = { /* TODO */ },
            onConfigureNotis   = onConfigureNotis,
            onChangeTheme      = { /* TODO */ },
            onToggleGlobalNotif= { /* TODO */ },
            onToggleTTS        = { /* TODO */ },
            onSignOut          = onSignOut,
            signingOut         = signingOut,
            onDeleteAccount    = { /* TODO */ },
            onOpenTerms        = { /* TODO */ },
            onOpenPrivacy      = { /* TODO */ },
        )
    }
}

/* ───────────────────────  Cuerpo  ──────────────────────────── */

@Composable
private fun SettingsBody(
    user               : User,
    /* cuenta */
    onEditPersonal     : () -> Unit,
    onDevices          : () -> Unit,
    onAchievements     : () -> Unit,
    onConfigureNotis   : () -> Unit,
    /* prefs */
    onChangeTheme      : (ThemeMode) -> Unit,
    onToggleGlobalNotif: (Boolean) -> Unit,
    onToggleTTS        : (Boolean) -> Unit,
    onOpenTerms        : () -> Unit,
    onOpenPrivacy      : () -> Unit,
    /* sesión */
    onSignOut          : () -> Unit,
    signingOut         : Boolean,
    onDeleteAccount    : () -> Unit,
) {
    /* diálogos locales */
    val snackbar = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cuenta") },
            text  = { Text("¿Seguro? Esta acción es irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteAccount(); showDeleteDialog = false }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton  = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    /* estado de switches */
    val settings = user.settings
    var theme     by remember { mutableStateOf(settings.theme) }
    var notifGlob by remember { mutableStateOf(settings.notifGlobal) }
    var tts       by remember { mutableStateOf(settings.accessibilityTTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /* ---------- header ---------- */
        SettingsHeader()

        /* ---------- Cuenta ---------- */
        FormContainer(backgroundColor = Color(0xFFD8EAF1)) {
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

        /* ---------- Preferencias ---------- */
        FormContainer(backgroundColor = Color(0xFFE8F2F8)) {
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.Palette) },
                text        = "Tema: ${theme.label()}",
                onClick     = { theme = theme.next().also(onChangeTheme) }
            )
            SwitchSettingItem(
                leadingIcon = { Icon24(Icons.Default.NotificationsActive) },
                text = "Notificaciones globales",
                checked = notifGlob,
                onCheckedChange = {
                    notifGlob = it
                    onToggleGlobalNotif(it)
                }
            )
            SwitchSettingItem(
                leadingIcon = { Icon24(Icons.Default.RecordVoiceOver) },
                text = "Texto a voz (TTS)",
                checked = tts,
                onCheckedChange = {
                    tts = it
                    onToggleTTS(it)
                }
            )
        }

        /* ---------- Legal ---------- */
        FormContainer(backgroundColor = Color(0xFFF3F6F8)) {
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

        /* ---------- Sesión ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DangerButton(
                text = if (signingOut) "Cerrando…" else "Cerrar sesión",
                onClick = onSignOut,
                enabled = !signingOut,
                modifier = Modifier.weight(1f)
            )
            DangerButton(
                text = "Eliminar cuenta",
                onClick = { showDeleteDialog = true },
                modifier = Modifier.weight(1f)
            )
        }
    }

    SnackbarHost(snackbar)
}

/* ───────────────────  Helper composables ───────────────────── */

@Composable
private fun SettingsHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ImageContainer(
            resId = R.drawable.ic_settings,
            contentDescription = "Ajustes",
            modifier = Modifier.size(104.dp)
        )
        Title(text = "Ajustes")
    }
}

@Composable
private fun SwitchSettingItem(
    leadingIcon : @Composable () -> Unit,
    text        : String,
    checked     : Boolean,
    onCheckedChange: (Boolean) -> Unit
) = SettingItem(
    leadingIcon = leadingIcon,
    text        = text,
    trailing    = { SwitchToggle(checked = checked, onCheckedChange = onCheckedChange) }
)

@Composable
private fun Icon24(icon: ImageVector) =
    Icon(icon, null, tint = Color(0xFF3EA8FE), modifier = Modifier.size(24.dp))

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

/* ---------- Center helper ---------- */
@Composable
private fun Centered(txt: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(txt)
    }
