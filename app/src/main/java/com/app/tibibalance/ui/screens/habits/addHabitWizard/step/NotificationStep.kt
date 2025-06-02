/*
 * Paso 3 – Configuración de las notificaciones
 *
 * • Horas de aviso (múltiples)
 * • Mensaje
 * • Fecha de inicio
 * • Modo (Sonido / Silencioso) + vibración
 * • Antelación
 * • Repetir recordatorio (opt-in con “checkbox”)
 */

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
import com.app.domain.enums.NotifMode
import com.app.domain.model.HabitForm
import com.app.tibibalance.ui.components.buttons.SwitchToggle
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalDatePickerDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputSelect
import com.app.tibibalance.ui.components.inputs.InputText
import com.app.tibibalance.ui.components.texts.Title
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationStep(
    form: HabitForm,
    onForm: (HabitForm) -> Unit
) {
    /* ------------ state ------------ */
    var local by remember(form) { mutableStateOf(form) }
    LaunchedEffect(local) { onForm(local) }

    var dlg       by remember { mutableStateOf<String?>(null) }
    var showTime  by remember { mutableStateOf(false) }
    var showDate  by remember { mutableStateOf(false) }
    var repeatChecked by remember { mutableStateOf(local.notifRepeatQty > 0) }
    /* Text Bindings (evitan repoblar “0/10” al borrar) */
    var qtyTxt    by remember(local.notifRepeatQty) {
        mutableStateOf(if (local.notifRepeatQty > 0) local.notifRepeatQty.toString() else "")
    }
    var snoozeTxt by remember(local.notifSnoozeMin) {
        mutableStateOf(if (local.notifSnoozeMin > 0) local.notifSnoozeMin.toString() else "")
    }

    val dateFmt = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }
    val scroll  = rememberScrollState()

    /* ------------ UI ------------ */
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Title("Notificaciones", Modifier.align(Alignment.CenterHorizontally))

        /* ──── Horas de recordatorio ──── */
        Header("Horas de recordatorio") { dlg = "hora" }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            local.notifTimes.sorted().forEach { hhmm ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(hhmm, style = MaterialTheme.typography.bodyLarge)
                    TextButton(
                        onClick = { local = local.copy(notifTimes = local.notifTimes - hhmm) }
                    ) { Text("Eliminar") }
                }
            }
            OutlinedButton(onClick = { showTime = true }) { Text("Añadir hora") }
        }

        /* ──── Mensaje ──── */
        Header("Mensaje") { dlg = "mensaje" }
        InputText(
            value         = local.notifMessage,
            onValueChange = { local = local.copy(notifMessage = it) },
            placeholder   = "¡Hora de completar tu hábito!",
            modifier      = Modifier.fillMaxWidth()
        )

        /* ──── Fecha de inicio ──── */
        Header("Fecha de inicio", null)
        OutlinedButton(
            onClick  = { showDate = true },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                local.notifStartsAt?.let { LocalDate.parse(it).format(dateFmt) }
                    ?: "Seleccionar fecha"
            )
        }

        /* ──── Modo ──── */
        Header("Modo de notificación", null)
        InputSelect(
            options        = listOf("Silencioso", "Sonido"),
            selectedOption = if (local.notifMode == NotifMode.SOUND) "Sonido" else "Silencioso",
            onOptionSelected = { sel ->
                local = local.copy(
                    notifMode = if (sel == "Sonido") NotifMode.SOUND else NotifMode.SILENT
                )
            }
        )

        /* ──── Vibración ──── */
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

        /* ──── Antelación ──── */
        Header("Minutos de antelación") { dlg = "adelanto" }
        var advTxt by remember(local.notifAdvanceMin) {
            mutableStateOf(if (local.notifAdvanceMin > 0) local.notifAdvanceMin.toString() else "")
        }
        InputText(
            value           = advTxt,
            onValueChange   = { txt ->
                advTxt = txt
                val n  = txt.toIntOrNull() ?: 0
                local = local.copy(notifAdvanceMin = n)
            },
            placeholder     = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.width(120.dp)
        )

        /* ──── Repetir recordatorio (opt-in) ──── */
        val repeatEnabled = local.notifRepeatQty > 0
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    repeatChecked = !repeatChecked                       // 🔄
                    if (!repeatChecked) {                                // se desactiva
                        qtyTxt = ""; snoozeTxt = ""
                        local  = local.copy(notifRepeatQty = 0, notifSnoozeMin = 0)
                    } else if (local.notifRepeatQty == 0) {              // se activa: valores por defecto
                        qtyTxt = "1"; snoozeTxt = "5"
                        local  = local.copy(notifRepeatQty = 1, notifSnoozeMin = 5)
                    }
                },
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Repetir recordatorio", style = MaterialTheme.typography.bodyMedium)
            Checkbox(
                checked = repeatChecked,                                // 🔄
                onCheckedChange = null                                  // manejado arriba
            )
        }

        AnimatedVisibility(repeatChecked) {                            // 🔄
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                /* Row 1 – veces */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InputText(
                        value           = qtyTxt,
                        onValueChange   = { txt ->
                            qtyTxt = txt
                            val n  = txt.toIntOrNull()
                            local  = local.copy(notifRepeatQty = n ?: 0)
                        },
                        placeholder     = "1",
                        isError         = (qtyTxt.isNotEmpty() && (qtyTxt.toIntOrNull() ?: 0) !in 1..5),
                        supportingText  = if (qtyTxt.isNotEmpty() &&
                            (qtyTxt.toIntOrNull() ?: 0 !in 1..5))
                            "1-5 máximo" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.width(72.dp)
                    )
                    Text("veces")
                }

                /* Row 2 – intervalo */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("cada")
                    InputText(
                        value           = snoozeTxt,
                        onValueChange   = { txt ->
                            snoozeTxt = txt
                            val n  = txt.toIntOrNull()
                            local = local.copy(notifSnoozeMin = n ?: 0)
                        },
                        placeholder     = "5",
                        isError         = (snoozeTxt.isNotEmpty() && (snoozeTxt.toIntOrNull() ?: 0) <= 0),
                        supportingText  = if (snoozeTxt.isNotEmpty() &&
                            (snoozeTxt.toIntOrNull() ?: 0) <= 0)
                            "≥ 1 min" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.width(72.dp)
                    )
                    Text("min")
                }
            }
        }
    }

    /* ───── TimePicker ───── */
    if (showTime) {
        val tp = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTime = false },
            title            = { Text("Selecciona hora") },
            text             = { TimePicker(tp) },
            confirmButton    = {
                TextButton(onClick = {
                    val hhmm = "%02d:%02d".format(tp.hour, tp.minute)
                    local = local.copy(
                        notifTimes = (local.notifTimes + hhmm).sorted().toSet()
                    )
                    showTime = false
                }) { Text("Aceptar") }
            },
            dismissButton    = {
                TextButton(onClick = { showTime = false }) { Text("Cancelar") }
            }
        )
    }

    /* ───── DatePicker ───── */
    ModalDatePickerDialog(
        visible         = showDate,
        initialDate     = local.notifStartsAt?.let(LocalDate::parse) ?: LocalDate.now(),
        title           = "Fecha de inicio",
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis >= LocalDate.now().toEpochDay() * 86_400_000L
        },
        onConfirmDate   = { picked ->
            showDate = false
            picked?.let { local = local.copy(notifStartsAt = it.toString()) }
        }
    )

    /* ───── Diálogos de ayuda ───── */
    when (dlg) {
        "hora"    -> infoDlg("Añade una o más horas para recibir recordatorios 🕒") { dlg = null }
        "mensaje" -> infoDlg("Este texto aparecerá en la notificación ✨")            { dlg = null }
        "adelanto"-> infoDlg(
            "Recibe una alerta X minutos antes del recordatorio principal ⏰"
        ) { dlg = null }
        "repetir" -> infoDlg(
            "Tras el recordatorio principal se enviarán N alertas extra cada M minutos. "
                    + "No deben solaparse con la siguiente hora configurada."
        ) { dlg = null }
    }
}

/* ───────────────────────────────────────────────────────────── */

@Composable
private fun Header(text: String, onInfo: (() -> Unit)?) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
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
        visible       = true,
        icon          = Icons.Default.Info,
        title         = "Ayuda",
        message       = message,
        primaryButton = DialogButton("Entendido", onDismiss)
    )
}
