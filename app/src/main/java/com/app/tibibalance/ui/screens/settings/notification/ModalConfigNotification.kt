@file:OptIn(ExperimentalMaterial3Api::class)

package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.domain.ids.HabitId
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.NotificationStep
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModalConfigNotification(
    habitId : HabitId,
    onDismiss: () -> Unit,
    vm: EditNotifViewModel = hiltViewModel()
) {
    /* -------- carga -------- */
    LaunchedEffect(habitId) { vm.load(habitId) }

    val form    by vm.form.collectAsState()
    val saving  by vm.saving.collectAsState()
    val savedOk by vm.savedOk.collectAsState()      // 🆕

    /* -------- cola de logros -------- */
    var currentAch by remember { mutableStateOf<AchievementUnlocked?>(null) }
    LaunchedEffect(Unit) {
        vm.unlocked.collect { ach -> if (currentAch == null) currentAch = ach }
    }

    val scope = rememberCoroutineScope()

    /* -------- bottom-sheet -------- */
    ModalContainer(
        onDismissRequest = { if (!saving) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalWindowInfo.current.containerSize.height.dp * .85f)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Box(Modifier.weight(1f)) {
                NotificationStep(form, vm::onFormChanged)
            }

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    text     = "Cancelar",
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled  = !saving
                )
                PrimaryButton(
                    text      = if (saving) "Guardando…" else "Guardar",
                    onClick   = { scope.launch { vm.save() } },
                    modifier  = Modifier.weight(1f),
                    enabled   = !saving && form.notify && form.notifTimes.isNotEmpty(),
                    isLoading = saving
                )
            }
        }
    }

    /* -------- modal de éxito -------- */
    if (savedOk) {
        ModalInfoDialog(
            visible = true,
            icon    = Icons.Default.Check,
            title   = "¡Cambios guardados!",
            message = "La configuración de notificaciones se actualizó correctamente.",
            primaryButton = DialogButton("Aceptar") {
                vm.consumeSaved()             // oculta este modal
                currentAch = vm.popNextAchievement() // muestra 1er logro si lo hay
                if (currentAch == null) {     // no hay logros → cerrar hoja
                    scope.launch {
                        awaitFrame()
                        onDismiss()
                    }
                }
            }
        )
    }

    /* -------- diálogo-logro en cadena -------- */
    currentAch?.let { ach ->
        ModalAchievementDialog(
            visible   = true,
            iconResId = R.drawable.ic_tibio_reloj,
            title     = "¡Logro desbloqueado!",
            message   = "${ach.name}\n${ach.description}",
            primaryButton = DialogButton("Aceptar") {
                val next = vm.popNextAchievement()
                if (next != null) {
                    currentAch = next          // siguiente de la cola
                } else {                       // cola vacía
                    currentAch = null
                    scope.launch {
                        awaitFrame()
                        onDismiss()
                    }
                }
            }
        )
    }
}