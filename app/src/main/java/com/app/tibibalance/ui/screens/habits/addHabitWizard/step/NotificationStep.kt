/* ui/screens/habits/addHabitWizard/step/NotificationStep.kt */
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
import com.app.domain.config.NotifConfig
import com.app.domain.enums.NotifMode
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalDatePickerDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputSelect
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.texts.Title
import kotlinx.datetime.LocalTime               // ← kotlinx-datetime, no java.time
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Paso ➌ — Configuración de las notificaciones.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationStep(
    title       : String,
    initialCfg  : NotifConfig,
    onCfgChange : (NotifConfig) -> Unit,
    onBack      : () -> Unit = {}
) {
    /* -------- estado -------- */
    var cfg by remember { mutableStateOf(initialCfg) }
    LaunchedEffect(cfg) { onCfgChange(cfg) }

    /* diálogos / pickers */
    var helperDlg by remember { mutableStateOf<String?>(null) }
    var showTime  by remember { mutableStateOf(false) }
    var showDate  by remember { mutableStateOf(false) }

    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }

    /* -------- UI principal -------- */
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Title("Notificaciones", Modifier.align(Alignment.CenterHorizontally))

        /* ---- Horas de recordatorio ---- */
        Header("Horas de recordatorio") { helperDlg = "hora" }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cfg.times.forEach { t ->
                val hhmm = t.toJavaLocalTime().format(timeFmt)   // ← conversión a java.time para formatear
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(hhmm, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = {
                        cfg = cfg.copy(times = cfg.times - t)
                    }) { Text("Eliminar") }
                }
            }
            OutlinedButton(onClick = { showTime = true }) { Text("Añadir hora") }
        }

        /* ---- Mensaje ---- */
        Header("Mensaje") { helperDlg = "mensaje" }
        InputText(
            value         = cfg.message,
            onValueChange = { cfg = cfg.copy(message = it) },
            placeholder   = "¡Hora de completar tu hábito!",
            modifier      = Modifier.fillMaxWidth()
        )

        /* ---- Fecha de inicio ---- */
        Header("Fecha de inicio", null)
        OutlinedButton(
            onClick  = { showDate = true },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(cfg.startsAt?.toJavaLocalDate()?.format(dateFmt) ?: "Seleccionar fecha")
        }

        /* ---- Modo ---- */
        Header("Modo de notificación", null)
        InputSelect(
            options        = listOf("Silencioso", "Sonido", "Vibrar"),
            selectedOption = when (cfg.mode) {
                NotifMode.SOUND   -> "Sonido"
                NotifMode.VIBRATE -> "Vibrar"
                else              -> "Silencioso"
            },
            onOptionSelected = { sel ->
                cfg = cfg.copy(
                    mode = when (sel) {
                        "Sonido" -> NotifMode.SOUND
                        "Vibrar" -> NotifMode.VIBRATE
                        else     -> NotifMode.SILENT
                    }
                )
            }
        )

        /* ---- Vibrar (solo Sonido) ---- */
        AnimatedVisibility(cfg.mode == NotifMode.SOUND) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { cfg = cfg.copy(vibrate = !cfg.vibrate) },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vibrar", style = MaterialTheme.typography.bodyMedium)
                SwitchToggle(
                    checked         = cfg.vibrate,
                    onCheckedChange = { cfg = cfg.copy(vibrate = it) }
                )
            }
        }

        /* ---- Antelación ---- */
        Header("Minutos de antelación") { helperDlg = "adelanto" }
        InputText(
            value           = cfg.advanceMin.takeIf { it > 0 }?.toString().orEmpty(),
            onValueChange   = { cfg = cfg.copy(advanceMin = it.toIntOrNull() ?: 0) },
            placeholder     = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.width(120.dp)
        )
    }

    /* -------- TimePicker -------- */
    if (showTime) {
        val tpState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            title            = { Text("Selecciona hora") },
            text             = { TimePicker(tpState) },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = LocalTime(tpState.hour, tpState.minute)     // kotlinx-datetime
                    cfg = cfg.copy(
                        times = (cfg.times + newTime)
                            .distinct()
                            .sortedBy { it.hour * 60 + it.minute }            // orden cronológico
                    )
                    showTime = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Cancelar") }
            }
        )
    }

    /* -------- DatePicker -------- */
    ModalDatePickerDialog(
        visible       = showDate,
        initialDate   = cfg.startsAt?.toJavaLocalDate() ?: LocalDate.now(),
        title         = "Fecha de inicio",
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis >= LocalDate.now().toEpochDay() * 86_400_000L
        },
        onConfirmDate = { picked ->
            showDate = false
            picked?.let { cfg = cfg.copy(startsAt = it.toKotlinLocalDate()) }
        }
    )

    /* -------- Diálogos de ayuda -------- */
    when (helperDlg) {
        "hora"     -> infoDlg("Puedes añadir varias horas dentro de un mismo día.") { helperDlg = null }
        "mensaje"  -> infoDlg("Este texto aparecerá en la notificación que recibas.") { helperDlg = null }
        "adelanto" -> infoDlg("Si necesitas tiempo para prepararte, indica cuántos minutos antes quieres que se muestre la notificación.") { helperDlg = null }
    }
}

/* ---------------- helpers ---------------- */

@Composable
private fun Header(text: String, onInfo: (() -> Unit)?) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
        onInfo?.let {
            IconButton(onClick = it) {
                Icon(Icons.Default.Info, contentDescription = "Información")
            }
        }
    }
}

@Composable
private fun infoDlg(message: String, onDismiss: () -> Unit) {
    ModalInfoDialog(
        visible       = true,
        icon          = Icons.Default.Info,
        title         = "Ayuda",
        message       = message,
        primaryButton = DialogButton("Entendido", onDismiss)
    )
}
