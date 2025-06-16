package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.utils.gradient

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfigureNotificationScreen(
    navController: NavHostController,
    viewModel   : ConfigureNotificationViewModel = hiltViewModel()
) {
    val ui            by viewModel.ui.collectAsState()
    val selectedHabit by viewModel.selectedHabit.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient())
    ) {
        /* ----- listado / estados ----- */
        when (ui) {
            ConfigureNotifUiState.Loading -> Centered("Cargando…")
            ConfigureNotifUiState.Empty   -> Centered("No tienes hábitos aún.")
            is ConfigureNotifUiState.Error ->
                Centered((ui as ConfigureNotifUiState.Error).msg)
            is ConfigureNotifUiState.Loaded -> HabitListSection(
                habits   = (ui as ConfigureNotifUiState.Loaded).data,
                onToggle = viewModel::toggleNotification,
                vm       = viewModel
            )
        }

        /* ----- Modal de edición de un hábito ----- */
        selectedHabit?.let { h ->
            ModalConfigNotification(
                habitId   = com.app.domain.ids.HabitId(h.id),
                onDismiss = { viewModel.clearSelectedHabit() }
            )
        }

        /* ----- Header ----- */
        Header(
            title          = "Notificaciones",
            showBackButton = true,
            onBackClick    = { navController.navigateUp() },
            modifier       = Modifier.align(Alignment.TopCenter)
        )
    }
}
