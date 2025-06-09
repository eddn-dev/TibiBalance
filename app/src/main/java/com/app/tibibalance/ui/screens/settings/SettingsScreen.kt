/* ui/screens/settings/SettingsScreen.kt */
package com.app.tibibalance.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.domain.entities.User
import com.app.domain.enums.ThemeMode
import com.app.tibibalance.tutorial.TutorialViewModel
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen

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
            syncing = ui.syncing,          // 👈 NUEVO
            onChangeTheme = vm::changeTheme,
            onToggleGlobalNotif = vm::toggleGlobalNotif,
            onToggleTTS = vm::toggleTTS,
            onSignOut = vm::signOut,
            onSyncAccount = vm::syncNow,   // 👈 NUEVO
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
    syncing             : Boolean,
    /* VM callbacks */
    onChangeTheme       : (ThemeMode) -> Unit,
    onToggleGlobalNotif : (Boolean) -> Unit,
    onToggleTTS         : (Boolean) -> Unit,
    onSignOut           : () -> Unit,
    onSyncAccount       : () -> Unit,
    vm: SettingsViewModel,
    ui: SettingsViewModel.UiState
) {
    val tutorialVm: TutorialViewModel = hiltViewModel()
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
        IconButton(onClick = { tutorialVm.restartTutorial() }, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Help, contentDescription = "Ayuda")
        }

        SettingsBody(
            ui = ui,
            vm = vm,
            user = user,
            onEditPersonal = onEditPersonal,
            onDevices = { /* TODO */ },
            onAchievements = { navController.navigate(Screen.Achievements.route) },
            onConfigureNotis = onConfigureNotis,
            onChangeTheme = onChangeTheme,
            onToggleGlobalNotif = onToggleGlobalNotif,
            onToggleTTS = onToggleTTS,
            onSignOut = onSignOut,
            signingOut = signingOut,
            onDeleteAccount = vm::reauthenticateAndDelete,
            onSyncAccount = onSyncAccount,
            syncing = syncing
        )

        ModalInfoDialog(
            visible  = ui.syncDone,
            icon     = Icons.Default.CheckCircle,
            title    = "¡Sincronización exitosa!",
            message  = "Tu cuenta se ha actualizado correctamente.",
            primaryButton = DialogButton(
                text = "Aceptar",
                onClick = vm::dismissSyncDone            // ⬅️ cierra modal
            )
        )
    }
}

/* ───────────────────── Helpers ───────────────────── */

@Composable
fun Icon24(icon: ImageVector) =
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary
        , modifier = Modifier.size(24.dp))

@Composable
fun SwitchSettingItem(
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
fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "Sistema"
    ThemeMode.LIGHT  -> "Claro"
    ThemeMode.DARK   -> "Oscuro"
}
fun ThemeMode.next() = when (this) {
    ThemeMode.SYSTEM -> ThemeMode.LIGHT
    ThemeMode.LIGHT  -> ThemeMode.DARK
    ThemeMode.DARK   -> ThemeMode.SYSTEM
}

@Composable
private fun Centered(msg: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(msg)
    }
