/* ui/screens/settings/ConfigureNotificationScreen.kt */
package com.app.tibibalance.ui.screens.settings.notification

import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.RoundedIconButton
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.IconContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.components.utils.gradient
import kotlinx.datetime.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfigureNotificationScreen(
    navController: NavHostController,
    viewModel: ConfigureNotificationViewModel = hiltViewModel()
) {
    /* ---------- UI-state ---------- */
    val ui by viewModel.ui.collectAsState()
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    selectedHabit?.let { h ->
        ModalConfigNotification(
            habitId = com.app.domain.ids.HabitId(h.id),
            onDismiss = { viewModel.clearSelectedHabit() }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient())
    ) {
        when (ui) {
            ConfigureNotifUiState.Loading -> Centered("Cargando…")
            ConfigureNotifUiState.Empty -> Centered("No tienes hábitos aún.")
            is ConfigureNotifUiState.Error ->
                Centered((ui as ConfigureNotifUiState.Error).msg)
            is ConfigureNotifUiState.Loaded -> HabitListSection(
                habits = (ui as ConfigureNotifUiState.Loaded).data,
                onToggle = viewModel::toggleNotification,
                vm = viewModel
            )
        }

        Header(
            title = "Notificaciones",
            showBackButton = true,
            onBackClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

    }
}

/* ───────────── lista con SettingItem ───────────── */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HabitListSection(
    habits: List<HabitNotifUi>,
    onToggle: (HabitNotifUi) -> Unit,
    vm: ConfigureNotificationViewModel = hiltViewModel()
) {
    // Estado local para controlar la visibilidad del modal “SABER MÁS”
    var showHelpModal by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 130.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /* encabezado */

        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant)
        {
            Subtitle(
                text = "Emociones",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        EmotionReminderSettingItem(vm)
        Spacer(Modifier.height(12.dp))
        FormContainer(backgroundColor = MaterialTheme.colorScheme.surfaceVariant)
        {
            Subtitle(
                text = "Hábitos",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        habits.forEach { habit ->
            SettingItem(
                leadingIcon = {
                    IconContainer(
                        icon = iconByName(habit.icon),
                        contentDescription = habit.name,
                        modifier = Modifier.size(24.dp),
                    )
                },
                text = habit.name,
                trailing = {
                    RoundedIconButton(
                        onClick = { onToggle(habit) },
                        icon = if (habit.enabled)
                            Icons.Default.NotificationsActive
                        else
                            Icons.Default.NotificationsOff,
                        modifier = Modifier.size(32.dp),
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.selectHabit(habit) }
            )
        }

        Spacer(Modifier.height(16.dp))

        /* Enlace “SABER MÁS” */
        TextButtonLink(
            text = "SABER MÁS",
            onClick = {
                showHelpModal = true
            }
        )
    }

    // ----------------- Modal personalizado “SABER MÁS” -----------------
    if (showHelpModal) {
        ModalContainer(
            onDismissRequest = { showHelpModal = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recuerda que los mensajes personalizados son esa" ,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "\uD83E\uDDE9frase motivadora\uD83C\uDFC6" ,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "que das de alta junto con tu hábito. ✨",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                ImageContainer(
                    resId = R.drawable.ic_settings,
                    contentDescription = "Icono de configuración",
                    modifier = Modifier.size(80.dp)

                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Puedes activar o desactivar las \nnotificaciones de tus hábitos," +
                            "\nsin alterar su configuración. 🔔",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/* helper para mensajes centrados */
@Composable
private fun Centered(msg: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(text = msg)
    }
@Composable
private fun SwitchSettingItem(
    leadingIcon: @Composable () -> Unit,
    text: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) = SettingItem(
    leadingIcon = leadingIcon,
    text = text,
    trailing = { SwitchToggle(checked = checked, onCheckedChange = { onCheckedChange() }) }
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EmotionReminderSettingItem(vm: ConfigureNotificationViewModel) {

    /*---- estados reactivos del VM ----*/
    val enabled  by vm.notifEmotion.collectAsState()
    val time     by vm.emotionTime.collectAsState()

    /*---- selector de hora modal (compose TimePicker) ----*/
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        val tp  = rememberTimePickerState(
            initialHour   = time?.hour   ?: 20,
            initialMinute = time?.minute ?: 0,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title  = { Text("Hora del recordatorio") },
            text   = { TimePicker(tp) },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateEmotionTime(LocalTime(tp.hour, tp.minute))
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        )
    }

    /*---- vista base ----*/
    SettingItem(
        leadingIcon = {
            IconContainer(
                icon  = Icons.Default.Mood,
                contentDescription = "Emociones",
                modifier = Modifier.size(24.dp)
            )
        },
        text = "Recordatorio de emociones",
        /* trailing: switch + hora ↓ */
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = time?.toString() ?: "--:--",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                SwitchToggle(
                    checked = enabled,
                    onCheckedChange = { vm.toggleEmotionNotif() }
                )
            }
        },
        /* 1 tap abre selector si está activado */
        onClick = { if (enabled) showPicker = true }
    )
}


