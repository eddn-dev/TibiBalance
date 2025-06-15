package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
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
import androidx.compose.ui.unit.dp
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.containers.IconContainer
import com.app.tibibalance.ui.components.utils.SettingItem
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmotionReminderSettingItem(vm: ConfigureNotificationViewModel) {

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


