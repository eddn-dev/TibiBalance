package com.app.tibibalance.ui.screens.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.ids.HabitId
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.EmptyState
import com.app.tibibalance.ui.components.utils.HabitList
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.screens.habits.addHabitWizard.AddHabitModal
import com.app.tibibalance.ui.screens.habits.editHabitWizard.EditHabitModal
import com.app.tibibalance.tutorial.rememberTutorialTarget
import com.app.tibibalance.tutorial.TutorialOverlay
import com.app.tibibalance.tutorial.TutorialViewModel
import kotlinx.coroutines.flow.collect

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitsScreen(
    vm: HabitsViewModel = hiltViewModel()
) {
    val tutorialVm: TutorialViewModel = hiltViewModel()

    // Estados locales para modales
    var showAdd by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<HabitId?>(null) }
    var showInfo by remember { mutableStateOf(false) }

    // Estado de UI del ViewModel de hábitos
    val ui by vm.uiState.collectAsState()

    // Paso actual de tutorial y target asociado
    val currentStep by tutorialVm.currentStep.collectAsState()
    val currentTargetId = currentStep?.targetId

    // Escucha eventos del VM de hábitos
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                HabitsEvent.AddClicked -> showAdd = true
                is HabitsEvent.ShowDetails -> editingId = HabitId(event.habitId)
            }
        }
    }

    // Inicia el tutorial si no se ha visto aún
    LaunchedEffect(Unit) {
        tutorialVm.startTutorialIfNeeded(Screen.Habits)
    }

    // ─── Contenido de la pantalla con los targets para el tutorial ─────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient())
            // Paso 0: resaltar toda el área de la lista
            .then(
                rememberTutorialTarget(
                    targetId = "habits_tab",
                    currentTargetId = currentTargetId,
                    onPositioned = tutorialVm::updateTargetBounds
                )
            )
    ) {
        when (val state = ui) {
            HabitsUiState.Loading -> Centered("Cargando…")
            is HabitsUiState.Error -> Centered(state.msg)
            HabitsUiState.Empty -> EmptyState(onAdd = { showAdd = true })
            is HabitsUiState.Loaded -> HabitList(
                habits = state.data,
                onEdit = vm::onHabitClicked,
                onAdd = { showAdd = true }
            )
        }

        // Paso 1: FAB de añadir hábito, con target para el tutorial
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .then(
                    rememberTutorialTarget(
                        targetId = "habit_fab",
                        currentTargetId = currentTargetId,
                        onPositioned = tutorialVm::updateTargetBounds
                    )
                )
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Añadir hábito")
        }

        // Botón de ayuda: reinicia tutorial en cualquier momento
        IconButton(
            onClick = { tutorialVm.restartTutorial(Screen.Habits) },
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd)
                .background(brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00DFF7),
                        Color(0xFF008EFF)
                    )
                ),
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tibio_tutorial),
                contentDescription = "Ayuda",
                modifier = Modifier.size(90.dp)
            )
        }

        // Link “SABER MÁS” siempre visible
        TextButtonLink(
            text = "SABER MÁS",
            onClick = { showInfo = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // Overlay de tutorial que dibuja highlights y, en el paso 1, el mini-video Lottie
    TutorialOverlay(viewModel = tutorialVm) {
        // (no hace falta contenido aquí; todo está en el Box de arriba)
    }

    // ─── Modales ─────────────────────────────────────────────────────────────────

    // Modal de Agregar hábito
    if (showAdd) {
        AddHabitModal(onDismiss = { showAdd = false })
    }

    // Modal de Editar hábito
    editingId?.let { habitId ->
        EditHabitModal(
            habitId = habitId,
            onDismiss = { editingId = null }
        )
    }

    // Modal informativo “SABER MÁS”
    if (showInfo) {
        ModalContainer(onDismissRequest = { showInfo = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = "Ícono de fuego",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Los hábitos en Modo Reto se identifican con un 🔥",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                ImageContainer(
                    resId = R.drawable.ic_tibio_arquitecto,
                    contentDescription = "Tibio Reto",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showInfo = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}
