package com.app.tibibalance.ui.screens.habits.addHabitWizard.step

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.domain.model.HabitForm
import com.app.domain.enums.NotifMode
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalDatePickerDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputSelect
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.texts.Title
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@ExperimentalMaterial3Api
@Composable
fun NotificationStep(
    form: HabitForm,
    onForm: (HabitForm) -> Unit
) {
    /* ------ estado local simplificado ------ */
    var local by remember(form) { mutableStateOf(form) }
    LaunchedEffect(local) { onForm(local) }

    /* diálogos */
    var dlg       by remember { mutableStateOf<String?>(null) }
    var showTime  by remember { mutableStateOf(false) }
    var showDate  by remember { mutableStateOf(false) }

    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Title("Notificaciones", Modifier.align(Alignment.CenterHorizontally))

        /* ---- lista de horas ---- */
        Header("Horas de recordatorio") { dlg = "hora" }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            local.notifTimes.sorted().forEach { hhmm ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(hhmm, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = {
                        local = local.copy(notifTimes = local.notifTimes - hhmm)
                    }) { Text("Eliminar") }
                }
            }
            OutlinedButton(onClick = { showTime = true }) { Text("Añadir hora") }
        }

        /* ---- mensaje ---- */
        Header("Mensaje") { dlg = "mensaje" }
        InputText(
            value         = local.notifMessage,
            onValueChange = { local = local.copy(notifMessage = it) },
            placeholder   = "¡Hora de completar tu hábito!",
            modifier      = Modifier.fillMaxWidth()
        )

        /* ---- fecha inicio ---- */
        Header("Fecha de inicio", null)
        OutlinedButton(
            onClick  = { showDate = true },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(local.notifStartsAt?.let {
                LocalDate.parse(it).format(dateFmt)
            } ?: "Seleccionar fecha")
        }

        /* ---- modo ---- */
        Header("Modo de notificación", null)
        val modeOptions = listOf("Silencioso", "Sonido")
        InputSelect(
            options        = modeOptions,
            selectedOption = if (local.notifMode == NotifMode.SOUND) "Sonido" else "Silencioso",
            onOptionSelected = { sel ->
                val mode = if (sel == "Sonido") NotifMode.SOUND else NotifMode.SILENT
                local = local.copy(
                    notifMode    = mode,
                    notifVibrate = local.notifVibrate && mode == NotifMode.SOUND
                )
            }
        )

        /* ---- vibrar ---- */
        AnimatedVisibility(local.notifMode == NotifMode.SOUND) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { local = local.copy(notifVibrate = !local.notifVibrate) },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vibrar", style = MaterialTheme.typography.bodyMedium)
                SwitchToggle(
                    checked         = local.notifVibrate,
                    onCheckedChange = { local = local.copy(notifVibrate = it) }
                )
            }
        }

        /* ---- antelación ---- */
        Header("Minutos de antelación") { dlg = "adelanto" }
        InputText(
            value           = local.notifAdvanceMin.takeIf { it > 0 }?.toString().orEmpty(),
            onValueChange   = { local = local.copy(notifAdvanceMin = it.toIntOrNull() ?: 0) },
            placeholder     = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.width(120.dp)
        )
    }

    /* ------ TimePicker ------ */
    if (showTime) {
        val tp = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Selecciona hora") },
            text  = { TimePicker(tp) },
            confirmButton = {
                TextButton(onClick = {
                    val hhmm = "%02d:%02d".format(tp.hour, tp.minute)
                    local = local.copy(notifTimes = (local.notifTimes + hhmm).sorted().toSet())
                    showTime = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Cancelar") }
            }
        )
    }

    /* ------ DatePicker ------ */
    ModalDatePickerDialog(
        visible       = showDate,
        initialDate   = local.notifStartsAt?.let(LocalDate::parse) ?: LocalDate.now(),
        title         = "Fecha de inicio",
        selectableDates = object : SelectableDates {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis >= LocalDate.now().toEpochDay() * 86_400_000L
        },
        onConfirmDate = { picked ->
            showDate = false
            picked?.let { local = local.copy(notifStartsAt = it.toString()) }
        }
    )

    /* ------ diálogos ayuda ------ */
    when (dlg) {
        "hora"     -> infoDlg("Añade una o más horas para recibir recordatorios.") { dlg = null }
        "mensaje"  -> infoDlg("Este texto aparecerá en la notificación.")          { dlg = null }
        "adelanto" -> infoDlg("Minutos antes del recordatorio para que te prepares.") { dlg = null }
    }
}

/* helpers ----------------------------------------------------------------- */

@Composable
private fun Header(text: String, onInfo: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
        onInfo?.let {
            IconButton(onClick = it) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
        }
    }
}

@Composable
private fun infoDlg(message: String, onDismiss: () -> Unit) {
    ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Info,
        title = "Ayuda",
        message = message,
        primaryButton = DialogButton("Entendido", onDismiss)
    )
}
