/*
 * @file    TrackingStep.kt
 * @ingroup ui_wizard_addHabit
 */
@file:OptIn(ExperimentalFoundationApi::class)

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.domain.config.RepeatPreset
import com.app.domain.model.HabitForm
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.*
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.ToggleRow
import kotlinx.collections.immutable.persistentSetOf

/**
 * Paso 2 — Seguimiento: duración, repetición, periodo y modo reto/notificaciones.
 *
 * @param form   Estado actual del formulario.
 * @param onForm Callback que envía las actualizaciones al ViewModel.
 */
@Composable
fun TrackingStep(
    form: HabitForm,
    onForm: (HabitForm) -> Unit
) {
    /* -------- estado editable local -------- */
    var localForm by remember(form) { mutableStateOf(form) }
    LaunchedEffect(localForm) { onForm(localForm) }

    /* -------- diálogos -------- */
    var dlg by remember { mutableStateOf<String?>(null) }

    /* -------- validaciones rápidas -------- */
    val sessionQtyErr = localForm.sessionUnit != SessionUnit.INDEFINIDO && localForm.sessionQty == null
    val periodQtyErr  = localForm.periodUnit  != PeriodUnit.INDEFINIDO  && localForm.periodQty  == null
    val weekDaysErr   = localForm.repeatPreset == RepeatPreset.PERSONALIZADO && localForm.weekDays.isEmpty()

    /* -------- helpers lógicos -------- */
    fun enforceRules(newForm: HabitForm): HabitForm {
        var f = newForm

        /* 1 ▸ Notificaciones solo si hay repetición */
        if (f.repeatPreset == RepeatPreset.INDEFINIDO && f.notify)
            f = f.copy(notify = false)

        /* 2 ▸ Challenge requiere repetición y periodo */
        if ((f.repeatPreset == RepeatPreset.INDEFINIDO ||
                    f.periodUnit   == PeriodUnit.INDEFINIDO) && f.challenge)
            f = f.copy(challenge = false)

        return f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Title("Parámetros de seguimiento", Modifier.align(Alignment.CenterHorizontally))

        /* ───── Duración de la sesión ───── */
        LabeledSection("Duración de la actividad", onInfo = { dlg = "duracion" }) {
            Row(
                Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(localForm.sessionUnit != SessionUnit.INDEFINIDO) {
                    InputNumber(
                        value          = localForm.sessionQty?.toString().orEmpty(),
                        onValueChange  = { qty ->
                            localForm = enforceRules(localForm.copy(sessionQty = qty.toIntOrNull()))
                        },
                        placeholder    = "Cantidad",
                        modifier       = Modifier.width(120.dp),
                        isError        = sessionQtyErr,
                        supportingText = if (sessionQtyErr) "Requerido" else null
                    )
                }

                val unitOptions = listOf("No aplica", "veces", "min", "hrs")
                InputSelect(
                    options        = unitOptions,
                    selectedOption = when (localForm.sessionUnit) {
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
                        localForm = enforceRules(
                            localForm.copy(
                                sessionUnit = unit,
                                sessionQty  = localForm.sessionQty.takeIf { unit != SessionUnit.INDEFINIDO }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    isError  = sessionQtyErr && localForm.sessionUnit != SessionUnit.INDEFINIDO
                )
            }
        }

        /* ───── Repetición ───── */
        LabeledSection("Repetir hábito", onInfo = { dlg = "repeat" }) {
            val repeatOptions = listOf(
                "No aplica", "Diario", "Cada 3 días",
                "Semanal", "Quincenal", "Mensual", "Personalizado"
            )
            InputSelect(
                options = repeatOptions,
                selectedOption = when (localForm.repeatPreset) {
                    RepeatPreset.DIARIO       -> "Diario"
                    RepeatPreset.CADA_3_DIAS   -> "Cada 3 días"
                    RepeatPreset.SEMANAL       -> "Semanal"
                    RepeatPreset.QUINCENAL     -> "Quincenal"
                    RepeatPreset.MENSUAL       -> "Mensual"
                    RepeatPreset.PERSONALIZADO -> "Personalizado"
                    else                       -> "No aplica"
                },
                onOptionSelected = { sel ->
                    val preset = when (sel) {
                        "Diario"        -> RepeatPreset.DIARIO
                        "Cada 3 días"   -> RepeatPreset.CADA_3_DIAS
                        "Semanal"       -> RepeatPreset.SEMANAL
                        "Quincenal"     -> RepeatPreset.QUINCENAL
                        "Mensual"       -> RepeatPreset.MENSUAL
                        "Personalizado" -> RepeatPreset.PERSONALIZADO
                        else            -> RepeatPreset.INDEFINIDO
                    }
                    localForm = enforceRules(
                        localForm.copy(
                            repeatPreset = preset,
                            weekDays     = if (preset == RepeatPreset.PERSONALIZADO)
                                localForm.weekDays
                            else persistentSetOf()
                        )
                    )
                },
                supportingText = if (weekDaysErr) "Elige al menos un día" else null
            )
        }

        /* Días de la semana */
        AnimatedVisibility(localForm.repeatPreset == RepeatPreset.PERSONALIZADO) {
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
                        val selected = day in localForm.weekDays
                        FilterChip(
                            selected = selected,
                            onClick  = {
                                localForm = enforceRules(
                                    if (selected)
                                        localForm.copy(weekDays = localForm.weekDays - day)
                                    else
                                        localForm.copy(weekDays = localForm.weekDays + day)
                                )
                            },
                            label = { Text(l) }
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

        /* ───── Periodo total ───── */
        LabeledSection("Periodo del hábito", onInfo = { dlg = "periodo" }) {
            Row(
                Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(localForm.periodUnit != PeriodUnit.INDEFINIDO) {
                    InputNumber(
                        value          = localForm.periodQty?.toString().orEmpty(),
                        onValueChange  = { qty ->
                            localForm = enforceRules(
                                localForm.copy(periodQty = qty.toIntOrNull())
                            )
                        },
                        placeholder    = "Cantidad",
                        modifier       = Modifier.width(120.dp),
                        isError        = periodQtyErr,
                        supportingText = if (periodQtyErr) "Requerido" else null
                    )
                }
                InputSelect(
                    options = listOf("No aplica", "días", "semanas", "meses"),
                    selectedOption = when (localForm.periodUnit) {
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
                        localForm = enforceRules(
                            localForm.copy(
                                periodUnit = unit,
                                periodQty  = localForm.periodQty.takeIf { unit != PeriodUnit.INDEFINIDO }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    isError  = periodQtyErr && localForm.periodUnit != PeriodUnit.INDEFINIDO
                )
            }
        }

        /* ───── Toggles ───── */
        if (localForm.repeatPreset != RepeatPreset.INDEFINIDO) {
            ToggleRow(
                label   = "Notificarme",
                checked = localForm.notify,
                onToggle = { localForm = localForm.copy(notify = !localForm.notify) }
            )
        }

        ToggleRow(
            label   = "Modo reto",
            checked = localForm.challenge,
            onToggle = {
                val ok = localForm.repeatPreset != RepeatPreset.INDEFINIDO &&
                        localForm.periodUnit   != PeriodUnit.INDEFINIDO
                if (ok) localForm = localForm.copy(challenge = !localForm.challenge)
                else    dlg = "reto"
            },
            trailing = {
                IconButton(onClick = { dlg = "reto" }) {
                    Icon(Icons.Default.Info, contentDescription = "Info modo reto")
                }
            }
        )
    }

    /* -------- diálogos de ayuda -------- */
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

/* ---------------- helpers ---------------- */

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
        onInfo?.let {
            IconButton(onClick = it) {
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
