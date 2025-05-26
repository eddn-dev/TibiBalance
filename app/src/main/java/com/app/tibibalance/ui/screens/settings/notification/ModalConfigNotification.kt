/* ui/screens/settings/ModalConfigNotification.kt */
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
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.NotificationStep

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ModalConfigNotification(
    habitId      : com.app.domain.ids.HabitId,
    onDismiss    : () -> Unit,
    vm           : EditNotifViewModel = hiltViewModel()
) {
    /* carga explícita */
    LaunchedEffect(habitId) { vm.load(habitId) }

    val form   by vm.form.collectAsState()
    val saving by vm.saving.collectAsState()

    /* contenedor 85 % alto pantalla */
    ModalContainer(
        onDismissRequest = { if (!saving) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .85f)
    ) {
        Column(Modifier.fillMaxSize()) {

            /* -------- cuerpo reutilizado -------- */
            Box(Modifier.weight(1f)) {
                NotificationStep(form = form, onForm = vm::onFormChanged)
            }
            /* -------- barra inferior -------- */
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
                        vm.save()
                        onDismiss()
                    },
                    enabled = !saving && form.notify && form.notifTimes.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
