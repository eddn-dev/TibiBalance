@file:OptIn(ExperimentalMaterial3Api::class)

package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.NotificationStep
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModalConfigNotification(
    habitId : HabitId,
    onDismiss: () -> Unit,
    vm: EditNotifViewModel = hiltViewModel()
) {
    /* ------------ carga inicial ------------- */
    LaunchedEffect(habitId) { vm.load(habitId) }

    /* ------------ state ------------- */
    val form   by vm.form.collectAsState()
    val saving by vm.saving.collectAsState()

    /* ------------ flujo de logros ------------- */
    var pendingAch by remember { mutableStateOf<AchievementUnlocked?>(null) }
    LaunchedEffect(Unit) { vm.unlocked.collect { pendingAch = it } }

    /* Para cerrar automáticamente cuando no hubo logro */
    var saveTriggered by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    ModalContainer(
        onDismissRequest = { if (!saving) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalWindowInfo.current.containerSize.height.dp * .85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {

            /* -------- cuerpo (paso de notificaciones) -------- */
            Box(Modifier.weight(1f)) {
                NotificationStep(form = form, onForm = vm::onFormChanged)
            }

            /* -------- botones -------- */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    text     = "Cancelar",
                    enabled  = !saving,
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text     = if (saving) "Guardando…" else "Guardar",
                    enabled  = !saving && form.notify && form.notifTimes.isNotEmpty(),
                    onClick  = {
                        saveTriggered = true
                        scope.launch { vm.save() }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    /* ------------ Modal de logro desbloqueado ------------ */
    pendingAch?.let { ach ->
        ModalAchievementDialog(
            visible      = true,
            iconResId    = R.drawable.ic_tibio_reloj,
            title        = "¡Logro desbloqueado!",
            message      = "${ach.name}\n${ach.description}",
            primaryButton = DialogButton("Aceptar") {
                pendingAch = null          // cierra modal
                onDismiss()                // y cierra el sheet
            }
        )
    }

    /* ------------ cierre automático si no hubo logro ------------ */
    LaunchedEffect(saving, pendingAch, saveTriggered) {
        if (saveTriggered && !saving && pendingAch == null) {
            saveTriggered = false
            onDismiss()
        }
    }
}
