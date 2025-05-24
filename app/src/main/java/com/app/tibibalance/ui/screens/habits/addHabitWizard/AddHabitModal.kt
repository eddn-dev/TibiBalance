@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.app.tibibalance.ui.screens.habits.addHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitModal(
    onDismiss: () -> Unit,
    vm: AddHabitViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    /* escucha evento Dismiss */
    LaunchedEffect(Unit) {
        vm.events.collect { if (it is WizardEvent.Dismiss) onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = { vm.requestExit() },
        dragHandle = {}
    ) {
        val pagerState = rememberPagerState(ui.currentStep) { 4 }
        LaunchedEffect(ui.currentStep) { pagerState.animateScrollToPage(ui.currentStep) }

        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SuggestionStep(
                        suggestions = vm.suggestions.collectAsState().value,
                        onSuggestion = vm::pickSuggestion,
                        onCustom    = vm::next
                    )

                    1 -> BasicInfoStep(
                        form   = ui.form,
                        onForm = { f -> vm.updateForm { f } }            // ✔️
                    )

                    2 -> TrackingStep(
                        form   = ui.form,
                        onForm = { f -> vm.updateForm { f } }            // ✔️
                    )

                    3 -> NotificationStep(
                        form   = ui.form,
                        onForm = { f -> vm.updateForm { f } }            // ✔️
                    )
                }
            }


            WizardNavBar(
                step      = ui.currentStep,
                stepValid = vm.isStepValid(ui.currentStep, ui.form),
                saving    = ui.saving,
                onBack    = vm::back,
                onNext    = vm::next,
                onSave    = vm::save
            )
        }
    }

    /* ---------- diálogos ---------- */
    if (ui.askExit) ConfirmDialog(
        title = "¿Salir sin guardar?",
        msg   = "Perderás la información introducida.",
        onYes = { vm.confirmExit(true) },
        onNo  = { vm.confirmExit(false) }
    )

    if (ui.askReplace) ConfirmDialog(
        title = "¿Reemplazar datos?",
        msg   = "Perderás los cambios actuales al aplicar la plantilla.",
        onYes = { vm.confirmReplace(true) },
        onNo  = { vm.confirmReplace(false) }
    )

    if (ui.savedOk) ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Check,
        title = "¡Listo!",
        message = "Hábito guardado con éxito.",
        primaryButton = DialogButton("Aceptar") { onDismiss() }
    )
}

/* ---------------- barra navegación ---------------- */

@Composable
private fun WizardNavBar(
    step: Int,
    stepValid: Boolean,
    saving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step > 0) TextButton(onClick = onBack) { Text("Atrás") }
        Spacer(Modifier.weight(1f))
        if (step < 3)
            Button(onClick = onNext, enabled = stepValid && !saving) { Text("Siguiente") }
        else
            Button(onClick = onSave, enabled = stepValid && !saving) { Text("Guardar") }
    }
}

/* ---------------- diálogo confirmación ---------------- */

@Composable
private fun ConfirmDialog(title: String, msg: String, onYes: () -> Unit, onNo: () -> Unit) {
    ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Warning,
        title = title,
        message = msg,
        primaryButton = DialogButton("Sí", onYes),
        secondaryButton = DialogButton("No", onNo)
    )
}
