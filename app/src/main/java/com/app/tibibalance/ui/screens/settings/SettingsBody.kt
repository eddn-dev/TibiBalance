package com.app.tibibalance.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.app.domain.entities.User
import com.app.domain.enums.ThemeMode
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.DangerButton
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.ConfirmDeleteDialog
import com.app.tibibalance.ui.components.dialogs.DeleteAccountDialog
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.SettingItem

@Composable
fun SettingsBody(
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
    onSyncAccount      : () -> Unit,
    /* Sesión */
    onSignOut          : () -> Unit,
    signingOut         : Boolean,
    syncing            : Boolean,
    onDeleteAccount: (String) -> Unit
) {
    /* diálogos */
    val snackbar = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val errorMessage = ui.error

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            visible = showDeleteDialog,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                showPasswordDialog = true
            }
        )
    }

    if (showPasswordDialog) {
        DeleteAccountDialog(
            visible = showPasswordDialog,
            onDismiss = { showPasswordDialog = false },
            onConfirm = {
                onDeleteAccount(it)
                showPasswordDialog = false
                showErrorDialog = true  // ← activa el modal de error si lo hay
            }
        )
        if (showErrorDialog && errorMessage != null) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error al eliminar cuenta") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }
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
            .padding(top = 96.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            .testTag("settings_section"),
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
            SettingItem(
                leadingIcon = { Icon24(Icons.Default.Sync) },
                text        = if (syncing) "Sincronizando…" else "Sincronizar cuenta",
                onClick     = onSyncAccount,
                trailing    = {
                    if (syncing)
                        CircularProgressIndicator(           // import androidx.compose.material3.*
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                }
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

    if (ui.error != null) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("Error al eliminar cuenta") },
            text = { Text(ui.error ?: "") },
            confirmButton = {
                TextButton(onClick = { vm.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }
}