@file:OptIn(ExperimentalFoundationApi::class)

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.utils.WizardNavBar
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.*
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitModal(
    onDismiss: () -> Unit,
    vm: AddHabitViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid


    /* ------------- escucha de cierre programático ------------- */
    var showAchievement by remember { mutableStateOf<AchievementUnlocked?>(null) }
    var showSavedOk by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is WizardEvent.Dismiss -> onDismiss()
                is WizardEvent.ShowAchievement -> showAchievement = event.logro
            }
        }
    }

    LaunchedEffect(ui.savedOk) {
        if (ui.savedOk) {
            showSavedOk = true
        }
    }

    fun handleSuccessDismiss(vm: AddHabitViewModel) {
        val next = vm.popNextAchievement()
        if (next != null) {
            showAchievement = next
            vm.consumeSaved() // esto borra ui.savedOk, pero no cierra aún
        } else {
            vm.acknowledgeSaved()
        }
    }

    /* ------------- alto máximo: 85 % pantalla ------------- */
    val maxH = LocalConfiguration.current.screenHeightDp.dp * .85f

    /* ------------- contenedor base ------------- */
    ModalContainer(
        onDismissRequest = { vm.requestExit() },
        modifier = Modifier
            .fillMaxWidth()                                       // ancho completo en móvil
            .heightIn(max = maxH)                                 // alto limitado
    ) {

        /* ---------- Pager ---------- */
        val pager = rememberPagerState(ui.currentStep) { 4 }

        LaunchedEffect(ui.currentStep) { pager.animateScrollToPage(ui.currentStep) }

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {

            HorizontalPager(
                state             = pager,
                userScrollEnabled = false,
                modifier          = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SuggestionStep(
                        suggestions = vm.suggestions.collectAsState().value,
                        onSuggestion = vm::pickSuggestion,
                        onCustom    = vm::next
                    )
                    1 -> BasicInfoStep (ui.form) { vm.updateForm(it) }
                    2 -> TrackingStep  (ui.form) { vm.updateForm(it) }
                    3 -> NotificationStep(ui.form) { vm.updateForm(it) }
                }
            }

            /* ---------- barra inferior ---------- */
            when (ui.currentStep) {
                0 -> PrimaryButton(
                    text = "Crear hábito personalizado",
                    onClick = vm::next
                )

                else -> WizardNavBar(
                    step      = ui.currentStep,
                    stepValid = vm.isStepValid(ui.currentStep, ui.form),
                    saving    = ui.saving,
                    notifyOn  = ui.form.notify,
                    onBack    = vm::back,
                    onNext    = vm::next,
                    onSave = {
                        //val userId = FirebaseAuth.getInstance().currentUser?.uid
                        //val context = LocalContext.current
                        if (userId != null) {
                            vm.save(context)
                        } else {
                            // puedes mostrar un diálogo de error o un toast si lo deseas
                        }
                    },
                    onCancel  = vm::requestExit
                )
            }
        }
    }

    /* ---------------- diálogos ---------------- */

    if (ui.askExit) ConfirmDialog(
        title = "¿Salir sin guardar?",
        msg   = "Perderás la información introducida.",
        onYes = { vm.confirmExit(true) },     // realmente cierra
        onNo  = { vm.confirmExit(false) }     // solo oculta diálogo
    )

    if (ui.askReplace) ConfirmDialog(
        title = "¿Reemplazar datos?",
        msg   = "Perderás los cambios actuales al aplicar la plantilla.",
        onYes = { vm.confirmReplace(true) },
        onNo  = { vm.confirmReplace(false) }
    )

    if (showSavedOk) {
        ModalInfoDialog(
            visible = true,
            icon = Icons.Default.Check,
            title = "¡Listo!",
            message = "Hábito guardado con éxito.",
            primaryButton = DialogButton("Aceptar") {
                showSavedOk = false
                handleSuccessDismiss(vm)
            }
        )
    }

    showAchievement?.let { logro ->
        val iconRes = when (logro.id) {
            "tibio_salud"         -> R.drawable.ic_tibio_salud
            "tibio_productividad" -> R.drawable.ic_tibio_productivo
            "tibio_bienestar"     -> R.drawable.ic_tibio_bienestar
            "primer_habito"       -> R.drawable.ic_tibio_explorer
            "cinco_habitos"       -> R.drawable.ic_tibio_arquitecto
            else                  -> R.drawable.avatar_placeholder
        }
        ModalAchievementDialog(
            visible = true,
            iconResId = iconRes,
            title = "¡Logro desbloqueado!",
            message = "${logro.name}\n${logro.description}",
            primaryButton = DialogButton("Aceptar") {
                val next = vm.popNextAchievement()
                if (next != null) {
                    showAchievement = next
                } else {
                    showAchievement = null
                    vm.acknowledgeSaved()
                }
            }
        )
    }

}

/* ---------------- barra nav para pasos 1-3 ---------------- */

@Composable
fun ConfirmDialog(
    title: String,
    msg: String,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Warning,
        title = title,
        message = msg,
        primaryButton = DialogButton("Sí", onYes),
        secondaryButton = DialogButton("No", onNo)
    )
}
