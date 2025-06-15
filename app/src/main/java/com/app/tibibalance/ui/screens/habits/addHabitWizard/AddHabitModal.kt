package com.app.tibibalance.ui.screens.habits.addHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.utils.WizardNavBar
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.BasicInfoStep
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.NotificationStep
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.SuggestionStep
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.TrackingStep
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import kotlinx.coroutines.android.awaitFrame

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitModal(
    onDismiss: () -> Unit,
    vm: AddHabitViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    /* ---------- escucha de cierre programático ---------- */
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            if (ev is WizardEvent.Dismiss) onDismiss()
        }
    }

    /* ---------- estado local ---------- */
    var showAchievement by remember { mutableStateOf<AchievementUnlocked?>(null) }
    var showSavedOk by remember { mutableStateOf(false) }

    var pendingShowAchievements by remember { mutableStateOf(false) }

    /* ---------- Logros desbloqueados ---------- */
    LaunchedEffect(Unit) {
        vm.unlocked.collect { ach ->
            if(!showSavedOk && showAchievement == null) {
                showAchievement = ach
            }
        }
    }

    /* cuando savedOk se pone false y había pending → muestra logros */
    LaunchedEffect(showSavedOk) {
        if(!showSavedOk){
            showAchievement = vm.popNextAchievement()
        }
    }

    /* ---------- contenedor base ---------- */
    val maxH = LocalWindowInfo.current.containerSize.height.dp * .85f
    ModalContainer(
        onDismissRequest = { vm.requestExit() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxH)
    ) {
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
                    1 -> BasicInfoStep (ui.form, vm::updateForm)
                    2 -> TrackingStep  (ui.form, vm::updateForm)
                    3 -> NotificationStep(ui.form, vm::updateForm)
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
                    onSave    = vm::save,
                    onCancel  = vm::requestExit
                )
            }
        }
    }

    /* ---------- diálogos de confirmación ---------- */
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

    /* ---------- diálogo “Hábito guardado” ---------- */
    if (showSavedOk) {
        ModalInfoDialog(
            visible = true,
            icon    = Icons.Default.Check,
            title   = "¡Listo!",
            message = "Hábito guardado con éxito.",
            primaryButton = DialogButton("Aceptar") {
                showSavedOk = false            // oculta modal
                pendingShowAchievements = true // 🆕 dispara la cola cuando se desmonte
            }
        )
    }

    /* ---------- Logros en cadena ---------- */
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
            visible     = true,
            iconResId   = iconRes,
            title       = "¡Logro desbloqueado!",
            message     = "${logro.name}\n${logro.description}",
            primaryButton = DialogButton("Aceptar") {
                val next = vm.popNextAchievement()
                if (next != null) showAchievement = next
                else {
                    showAchievement = null
                    vm.acknowledgeSaved()
                }
            }
        )
    }

    /* ---------- diálogo de éxito desde VM ---------- */
    LaunchedEffect(ui.savedOk) {
        if (ui.savedOk) {
            showSavedOk = true        // muestra modal éxito
            vm.consumeSaved()         // limpia flag en el VM
        }
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
