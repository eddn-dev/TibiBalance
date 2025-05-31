/* ui/screens/settings/ConfigureNotificationScreen.kt */
package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.RoundedIconButton
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.containers.IconContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.utils.SettingItem

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfigureNotificationScreen(
    navController: NavHostController,
    viewModel: ConfigureNotificationViewModel = hiltViewModel()
) {
    /* ---------- fondo ---------- */
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    /* ---------- UI-state ---------- */
    val ui by viewModel.ui.collectAsState()
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    selectedHabit?.let { h ->
        ModalConfigNotification(
            habitId = com.app.domain.ids.HabitId(h.id),
            onDismiss = { viewModel.clearSelectedHabit() }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient)
    ) {
        when (ui) {
            ConfigureNotifUiState.Loading -> Centered("Cargando…")
            ConfigureNotifUiState.Empty -> Centered("No tienes hábitos aún.")
            is ConfigureNotifUiState.Error ->
                Centered((ui as ConfigureNotifUiState.Error).msg)
            is ConfigureNotifUiState.Loaded -> HabitListSection(
                habits = (ui as ConfigureNotifUiState.Loaded).data,
                onToggle = viewModel::toggleNotification,
                vm = viewModel
            )
        }

        Header(
            title = "Notificaciones",
            showBackButton = true,
            onBackClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

    }
}

/* ───────────── lista con SettingItem ───────────── */

@Composable
private fun HabitListSection(
    habits: List<HabitNotifUi>,
    onToggle: (HabitNotifUi) -> Unit,
    vm: ConfigureNotificationViewModel = hiltViewModel()
) {
    // Estado local para controlar la visibilidad del modal “SABER MÁS”
    var showHelpModal by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 130.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /* encabezado */
        FormContainer(backgroundColor = Color(0xFFAED3E3)) {
            Subtitle(
                text = "Hábitos",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.height(12.dp))

        habits.forEach { habit ->
            SettingItem(
                leadingIcon = {
                    IconContainer(
                        icon = iconByName(habit.icon),
                        contentDescription = habit.name,
                        modifier = Modifier.size(24.dp),
                    )
                },
                text = habit.name,
                trailing = {
                    RoundedIconButton(
                        onClick = { onToggle(habit) },
                        icon = if (habit.enabled)
                            Icons.Default.NotificationsActive
                        else
                            Icons.Default.NotificationsOff,
                        modifier = Modifier.size(32.dp),
                    )
                },
                containerColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.selectHabit(habit) }
            )
        }

        Spacer(Modifier.height(16.dp))

        /* Enlace “SABER MÁS” */
        TextButtonLink(
            text = "SABER MÁS",
            onClick = {
                showHelpModal = true
            }
        )
    }

    // ----------------- Modal personalizado “SABER MÁS” -----------------
    if (showHelpModal) {
        ModalContainer(
            onDismissRequest = { showHelpModal = false }
        ) {
            // 1. Texto explicativo
            Text(
                text = "Recuerda que los mensajes personalizados son esa frase motivadora " +
                        "que diste de alta junto con tu hábito.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Imagen representativa de “configuración”
            ImageContainer(
                resId = R.drawable.ic_settings,
                contentDescription = "Icono de configuración",
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Texto adicional sobre notificaciones
            Text(
                text = "Puedes activar o desactivar las notificaciones de tus hábitos, " +
                        "sin alterar su configuración.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Botón para cerrar el modal
            Button(
                onClick = { showHelpModal = false },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Entendido")
            }
        }
    }
}

/* helper para mensajes centrados */
@Composable
private fun Centered(msg: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(text = msg)
    }
