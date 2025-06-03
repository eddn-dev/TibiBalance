@file:OptIn(ExperimentalMaterial3Api::class)

package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.NotificationStep
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModalConfigNotification(
    habitId: com.app.domain.ids.HabitId,
    onDismiss: () -> Unit,
    vm: EditNotifViewModel = hiltViewModel()
) {
    LaunchedEffect(habitId) { vm.load(habitId) }

    val form by vm.form.collectAsState()
    val saving by vm.saving.collectAsState()
    val logro by vm.logroDesbloqueado.collectAsState()

    val scope = rememberCoroutineScope()

    ModalContainer(
        onDismissRequest = { if (!saving) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Box(Modifier.weight(1f)) {
                NotificationStep(form = form, onForm = vm::onFormChanged)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !saving,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (saving) "Guardando…" else "Guardar",
                    onClick = {
                        scope.launch {
                            vm.guardarYVerificarLogro()
                        }
                    },
                    enabled = !saving && form.notify && form.notifTimes.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Modal de logro desbloqueado
    logro?.let {
        ModalAchievementDialog(
            visible = true,
            iconResId = R.drawable.ic_tibio_reloj,
            title = "¡Logro desbloqueado!",
            message = "${it.name}\n${it.description}",
            primaryButton = DialogButton("Aceptar") {
                vm.ocultarLogro()
                onDismiss() // cerrar al aceptar el logro
            }
        )
    }

    // Si no hubo logro, cerrar después de guardar
    LaunchedEffect(logro) {
        if (logro == null && vm.wasSaveTriggered && !saving) {
            onDismiss()
        }
    }
}