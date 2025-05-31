/* ─────────────────────────────────────────────────────────────
 *  EditHabitModal.kt
 *  Visualiza un hábito en modo lectura y, opcionalmente,
 *  permite editarlo con el mismo wizard de alta.
 *
 *  Patrón: “carga explícita”.
 *  - El VM es @HiltViewModel  (@Inject, sin Assisted-Inject).
 *  - El composable le pasa el habitId con  vm.load(habitId)
 *    cada vez que se muestra el modal.
 * ───────────────────────────────────────────────────────────── */
package com.app.tibibalance.ui.screens.habits.editHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.ids.HabitId
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.WizardNavBar
import com.app.tibibalance.ui.screens.habits.addHabitWizard.ConfirmDialog
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.*
import com.app.tibibalance.ui.screens.habits.editHabitWizard.step.ShowHabitStep

/* -------------------------------------------------------------------- */
/*  Composable                                                          */
/* -------------------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditHabitModal(
    habitId  : HabitId,
    onDismiss: () -> Unit,
    vm       : EditHabitViewModel = hiltViewModel()
) {
    LaunchedEffect(habitId) { vm.load(habitId) }

    /* ---------- state ---------- */
    val ui            by vm.ui.collectAsState()
    val data          by vm.habit.collectAsState()
    val challengeOn   by vm.challengeActive.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { if (it is EditEvent.Dismiss) onDismiss() }
    }

    /* ---------- container ---------- */
    ModalContainer(
        onDismissRequest = vm::requestExit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
    ) {
        if (data == null) { Centered("Cargando…"); return@ModalContainer }

        val pageCount = 4      // 0 y 3 solamente
        val pager     = rememberPagerState(ui.currentStep) { pageCount }

        LaunchedEffect(ui.currentStep) {
            /* evita crash si currentStep ya no existe */
            if (ui.currentStep >= pageCount) {
                vm.back()           // vuelve a 0
            } else pager.animateScrollToPage(ui.currentStep)
        }

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {

            /* ---------- páginas ---------- */
            HorizontalPager(
                state = pager,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> ShowHabitStep(
                        habit         = data!!,
                        onEditNotif   = vm::jumpToNotif,
                        onToggleNotif = vm::toggleNotifications,
                        onDelete      = vm::requestDelete,
                        deleting      = ui.deleting
                    )
                    1 -> if (!challengeOn) BasicInfoStep(ui.form, vm::updateForm)
                    else Spacer(Modifier.fillMaxSize())   // hueco invisible
                    2 -> if (!challengeOn) TrackingStep(ui.form, vm::updateForm)
                    else Spacer(Modifier.fillMaxSize())
                    3 -> NotificationStep(ui.form, vm::updateForm)
                }
            }

            /* ---------- bottom bar ---------- */
            if (ui.showOnly) {
                ReadOnlyBar(
                    onClose = { vm.requestExit() },
                    onEdit  = vm::startEditing
                )
            } else {
                WizardNavBar(
                    step        = ui.currentStep,
                    stepValid   = vm.isStepValid(ui.currentStep, ui.form),
                    saving      = ui.saving,
                    notifyOn    = ui.form.notify,
                    onBack      = vm::back,
                    onNext      = if (!challengeOn) vm::next else null,   // oculto en reto
                    onSave      = vm::save,
                    onCancel    = vm::requestExit
                )

            }

            /* confirmación de salida sin guardar */
            if (ui.askExit) {
                ConfirmDialog(
                    title = "¿Salir sin guardar?",
                    msg   = "Perderás los cambios realizados.",
                    onYes = { vm.confirmExit(true) },
                    onNo  = { vm.confirmExit(false) }
                )
            }

            if (ui.askDelete) {
                ConfirmDialog(
                    title = "¿Eliminar hábito?",
                    msg   = "Esta acción no se puede deshacer.",
                    onYes = { vm.confirmDelete(true) },
                    onNo  = { vm.confirmDelete(false) }
                )
            }
        }
    }
}


/* -------------------------------------------------------------------- */
/*  Barra inferior en modo lectura                                      */
/* -------------------------------------------------------------------- */
@Composable
private fun ReadOnlyBar(
    onClose: () -> Unit,
    onEdit : () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecondaryButton(text = "Editar", onClick = onEdit)
        PrimaryButton(text = "Listo", onClick = onClose)
    }
}
