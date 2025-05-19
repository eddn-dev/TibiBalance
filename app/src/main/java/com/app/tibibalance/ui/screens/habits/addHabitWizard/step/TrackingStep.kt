/* ui/screens/habits/addHabitWizard/step/TrackingStep.kt */
package com.app.tibibalance.ui.screens.habits.addHabitWizard.step

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.domain.config.RepeatPreset
import com.app.domain.entities.HabitForm
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.*
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.ToggleRow
import com.app.tibibalance.ui.screens.habits.addHabitWizard.HabitFormSaver
import kotlinx.collections.immutable.persistentSetOf

/**
 * Paso ➋ — Parámetros de repetición y reto.
 */
@OptIn(ExperimentalFoundationApi::class) // FlowRow es experimental
@Composable
fun TrackingStep(
    initial      : HabitForm,
    errors       : List<String>,
    onFormChange : (HabitForm) -> Unit,
    onBack       : () -> Unit = {}
) {
    var form by rememberSaveable(stateSaver = HabitFormSaver) { mutableStateOf(initial) }
    LaunchedEffect(form) { onFormChange(form) }

    /* Diálogos de ayuda */
    var dlg by remember { mutableStateOf<String?>(null) }

    /* Flags de error rápidos */
    val sessionQtyErr = errors.any { it.contains("duración", true) }
    val periodQtyErr  = errors.any { it.contains("periodo",  true) }
    val weekDaysErr   = errors.any { it.contains("día",      true) }
    val repeatErr     = errors.any { it.contains("repetición", true) }
    val unitOptions   = listOf("No aplica", "min", "hrs", "veces")

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Title("Parámetros de seguimiento", Modifier.align(Alignment.CenterHorizontally))

        /* -------- Duración de la sesión -------- */
        LabeledSection(
            label = "Duración de la actividad",
            onInfo = { dlg = "duracion" }
        ) {
            Row(
                Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(form.sessionUnit != SessionUnit.INDEFINIDO) {
                    InputNumber(
                        value          = form.sessionQty?.toString().orEmpty(),
                        onValueChange  = { form = form.copy(sessionQty = it.toIntOrNull()) },
                        placeholder    = "Cantidad",
                        modifier       = Modifier.width(120.dp),
                        isError        = sessionQtyErr,
                        supportingText = if (sessionQtyErr) "Requerido" else null
                    )
                }
                /* lista de opciones */
                val unitOptions = listOf("No aplica", "veces", "min", "hrs")

                InputSelect(
                    options = unitOptions,
                    selectedOption = when (form.sessionUnit) {
                        SessionUnit.VECES   -> "veces"
                        SessionUnit.MINUTOS -> "min"
                        SessionUnit.HORAS   -> "hrs"
                        else                -> "No aplica"
                    },
                    onOptionSelected = { sel ->
                        val unit = when (sel) {
                            "veces" -> SessionUnit.VECES
                            "min"   -> SessionUnit.MINUTOS
                            "hrs"   -> SessionUnit.HORAS
                            else    -> SessionUnit.INDEFINIDO
                        }
                        form = form.copy(
                            sessionUnit = unit,
                            sessionQty  = form.sessionQty.takeIf { unit != SessionUnit.INDEFINIDO }
                        )
                    },
                modifier = Modifier.weight(1f),
                    isError  = sessionQtyErr && form.sessionUnit != SessionUnit.INDEFINIDO
                )
            }
        }

        /* -------- Repetición -------- */
        LabeledSection("Repetir hábito", onInfo = { dlg = "repeat" }) {
            InputSelect(
                options = listOf(
                    "No aplica", "Diario", "Cada 3 días",
                    "Semanal", "Quincenal", "Mensual", "Personalizado"
                ),
                selectedOption = when (form.repeatPreset) {
                    RepeatPreset.DIARIO        -> "Diario"
                    RepeatPreset.CADA_3_DIAS    -> "Cada 3 días"
                    RepeatPreset.SEMANAL        -> "Semanal"
                    RepeatPreset.QUINCENAL      -> "Quincenal"
                    RepeatPreset.MENSUAL        -> "Mensual"
                    RepeatPreset.PERSONALIZADO  -> "Personalizado"
                    else                        -> "No aplica"
                },
                onOptionSelected = { sel ->
                    form = form.copy(
                        repeatPreset = when (sel) {
                            "Diario"        -> RepeatPreset.DIARIO
                            "Cada 3 días"   -> RepeatPreset.CADA_3_DIAS
                            "Semanal"       -> RepeatPreset.SEMANAL
                            "Quincenal"     -> RepeatPreset.QUINCENAL
                            "Mensual"       -> RepeatPreset.MENSUAL
                            "Personalizado" -> RepeatPreset.PERSONALIZADO
                            else            -> RepeatPreset.INDEFINIDO
                        },
                        weekDays = if (sel == "Personalizado") form.weekDays else persistentSetOf()
                    )
                },
                isError        = repeatErr,
                supportingText = if (repeatErr) "Requerido para modo reto" else null
            )
        }

        /* Días de la semana si es PERSONALIZADO */
        AnimatedVisibility(form.repeatPreset == RepeatPreset.PERSONALIZADO) {
            Column {
                Text("Días de la semana", style = MaterialTheme.typography.bodyMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    val labels = listOf("L","M","X","J","V","S","D")
                    labels.forEachIndexed { idx, l ->
                        val day = idx + 1
                        val selected = day in form.weekDays
                        FilterChip(
                            selected = selected,
                            onClick  = {
                                form = if (selected)
                                    form.copy(weekDays = form.weekDays - day)
                                else
                                    form.copy(weekDays = form.weekDays + day)
                            },
                            label    = { Text(l) }
                        )
                    }
                }
                if (weekDaysErr)
                    Text(
                        "Selecciona al menos un día",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
                    )
            }
        }

        /* -------- Periodo total -------- */
        LabeledSection("Periodo del hábito", onInfo = { dlg = "periodo" }) {
            Row(
                Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(form.periodUnit != PeriodUnit.INDEFINIDO) {
                    InputNumber(
                        value          = form.periodQty?.toString().orEmpty(),
                        onValueChange  = { form = form.copy(periodQty = it.toIntOrNull()) },
                        placeholder    = "Cantidad",
                        modifier       = Modifier.width(120.dp),
                        isError        = periodQtyErr,
                        supportingText = if (periodQtyErr) "Requerido" else null
                    )
                }
                InputSelect(
                    options = listOf("No aplica", "días", "semanas", "meses"),
                    selectedOption = when (form.periodUnit) {
                        PeriodUnit.DIAS    -> "días"
                        PeriodUnit.SEMANAS -> "semanas"
                        PeriodUnit.MESES   -> "meses"
                        else               -> "No aplica"
                    },
                    onOptionSelected = { sel ->
                        val unit = when (sel) {
                            "días"    -> PeriodUnit.DIAS
                            "semanas" -> PeriodUnit.SEMANAS
                            "meses"   -> PeriodUnit.MESES
                            else      -> PeriodUnit.INDEFINIDO
                        }
                        form = form.copy(
                            periodUnit = unit,
                            periodQty  = form.periodQty.takeIf { unit != PeriodUnit.INDEFINIDO }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    isError  = periodQtyErr && form.periodUnit != PeriodUnit.INDEFINIDO
                )
            }
        }

        /* -------- Toggles -------- */
        if (form.repeatPreset != RepeatPreset.INDEFINIDO) {
            ToggleRow(
                label   = "Notificarme",
                checked = form.notify,
                onToggle = { form = form.copy(notify = !form.notify) }
            )
        }
        ToggleRow(
            label   = "Modo reto",
            checked = form.challenge,
            onToggle = {
                val ok = form.repeatPreset   != RepeatPreset.INDEFINIDO &&
                        form.periodUnit     != PeriodUnit.INDEFINIDO
                if (ok) form = form.copy(challenge = !form.challenge)
                else    dlg = "reto"
            },
            trailing = {
                IconButton(onClick = { dlg = "reto" }) {
                    Icon(Icons.Default.Info, contentDescription = "Info modo reto")
                }
            }
        )
    }

    /* -------- Diálogos de ayuda -------- */
    when (dlg) {
        "reto" -> ModalInfoDialog(
            visible       = true,
            title         = "Modo reto",
            message       = "Para activar el modo reto debes definir repetición y periodo.",
            icon          = Icons.Default.Info,
            primaryButton = DialogButton("Entendido") { dlg = null }
        )
        "duracion" -> infoDialog("Duración de la actividad") { dlg = null }
        "repeat"   -> infoDialog("Repetir hábito")           { dlg = null }
        "periodo"  -> infoDialog("Periodo del hábito")       { dlg = null }
    }
}

/* Helpers ------------------------------------------------------------------ */

@Composable
private fun LabeledSection(
    label: String,
    onInfo: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (onInfo != null) {
            IconButton(onClick = onInfo) {
                Icon(Icons.Default.Info, contentDescription = "Información")
            }
        }
    }
    content()
}

@Composable
private fun infoDialog(title: String, onDismiss: () -> Unit) {
    ModalInfoDialog(
        visible       = true,
        icon          = Icons.Default.Info,
        title         = title,
        message       = "",
        primaryButton = DialogButton("Entendido", onDismiss)
    )
}
