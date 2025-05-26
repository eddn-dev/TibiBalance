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
import androidx.navigation.NavHostController
import com.app.domain.entities.User
import com.app.domain.enums.ThemeMode
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.DangerButton
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.navigation.Screen

/* ─────────────────────────  Public entry  ─────────────────────── */

@Composable
fun SettingsScreen(
    user                 : User,
    navController        : NavHostController,
    onNavigateUp         : () -> Unit,
    /* ─── cuenta ─── */
    onEditPersonal: () -> Unit = {
        navController.navigate(Screen.EditProfile.route)
    },
    onDevices            : () -> Unit,
    onAchievements       : () -> Unit,
    onConfigureNotis     : () -> Unit = {
        navController.navigate(Screen.ConfigureNotif.route)
    },
    /* ─── sesión ─── */
    onSignOut            : () -> Unit,
    onDeleteAccount      : () -> Unit,
    /* ─── preferencias ─── */
    onChangeTheme        : (ThemeMode) -> Unit,
    onToggleGlobalNotif  : (Boolean) -> Unit,
    onToggleTTS          : (Boolean) -> Unit,
    /* ─── legal ─── */
    onOpenTerms          : () -> Unit,
    onOpenPrivacy        : () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title  = { Text("Eliminar cuenta") },
            text   = { Text("¿Seguro? Esta acción es irreversible.") },
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

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient)
    ) {
        ReadyContent(
            user               = user,
            onEditPersonal     = onEditPersonal,
            onDevices          = onDevices,
            onAchievements     = onAchievements,
            onConfigureNotis   = onConfigureNotis,
            onSignOut          = onSignOut,
            onDeleteClick      = { showDeleteDialog = true },
            onChangeTheme      = onChangeTheme,
            onToggleGlobalNotif= onToggleGlobalNotif,
            onToggleTTS        = onToggleTTS,
            onOpenTerms        = onOpenTerms,
            onOpenPrivacy      = onOpenPrivacy,
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

/* ───────────────────────  Content  ───────────────────────────── */

@Composable
private fun ReadyContent(
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
    onDeleteClick      : () -> Unit,
) {
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

        /* ---------- grupo: Cuenta ---------- */
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

        /* ---------- grupo: Preferencias ---------- */
        FormContainer(backgroundColor = Color(0xFFE8F2F8)) {
            /* Tema */
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.Palette) },
                text        = "Tema: ${theme.label()}",
                onClick     = { theme = theme.next().also(onChangeTheme) }
            )
            /* Notif global */
            SwitchSettingItem(
                leadingIcon = { Icon24(Icons.Default.NotificationsActive) },
                text = "Notificaciones globales",
                checked = notifGlob,
                onCheckedChange = { notifGlob = it; onToggleGlobalNotif(it) }
            )
            /* TTS */
            SwitchSettingItem(
                leadingIcon = { Icon24(Icons.Default.RecordVoiceOver) },
                text = "Texto a voz (TTS)",
                checked = tts,
                onCheckedChange = { tts = it; onToggleTTS(it) }
            )
        }

        /* ---------- grupo: Legal ---------- */
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
                text = "Cerrar sesión",
                onClick = onSignOut,
                modifier = Modifier.weight(1f)
            )
            DangerButton(
                text = "Eliminar cuenta",
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/* ───────────────────── helper composables ───────────────────── */

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

/* Fila con Switch usando tu SwitchToggle */
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

/* Icon utilitario 24 dp */
@Composable
private fun Icon24(icon: ImageVector) =
    Icon(icon, contentDescription = null, tint = Color(0xFF3EA8FE), modifier = Modifier.size(24.dp))

/* ───────────────────  Extensiones  ──────────────────────────── */

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
