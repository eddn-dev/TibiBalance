/**
 * @file      HabitsScreen.kt
 * @ingroup   ui_screens_habits
 * @brief     Pantalla principal de hábitos con lista, estado vacío, carga y modal informativo.
 *
 * Este archivo define la pantalla `HabitsScreen`, la cual:
 *  - Observa el estado `uiState` proveniente de [HabitsViewModel] y muestra:
 *      • Una vista de carga si el estado es Loading.
 *      • Un mensaje de error si ocurre algún problema.
 *      • Un estado vacío con invitación a agregar hábitos si no hay datos.
 *      • La lista de hábitos cuando el estado es Loaded.
 *  - Maneja eventos de usuario para:
 *      • Agregar un nuevo hábito (despliega [AddHabitModal]).
 *      • Editar un hábito existente (despliega [EditHabitModal]).
 *  - Incluye un enlace de texto “SABER MÁS” siempre visible en la parte inferior, que
 *    despliega un modal explicativo: “Los hábitos en modo reto se identifican con un 🔥”,
 *    precedido de un ícono redondeado.
 *
 * @see HabitsViewModel
 * @see AddHabitModal
 * @see EditHabitModal
 * @see ModalContainer
 */

package com.app.tibibalance.ui.screens.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.app.tibibalance.ui.screens.habits.addHabitWizard.AddHabitModal
import com.app.tibibalance.ui.screens.habits.editHabitWizard.EditHabitModal

/**
 * @brief Pantalla principal de hábitos.
 *
 * Observa el estado `uiState` desde el [HabitsViewModel] y muestra:
 *  - Una vista de carga si el estado es Loading.
 *  - Un mensaje de error si ocurre algún error.
 *  - Un estado vacío con invitación a agregar hábitos si no hay datos (Empty).
 *  - La lista de hábitos cuando está Loaded.
 *
 * Además:
 *  - Maneja eventos para mostrar el modal de agregar hábito ([AddHabitModal]).
 *  - Al pulsar “Editar” en un hábito, muestra el modal de edición ([EditHabitModal]).
 *  - Siempre deja un enlace de texto “SABER MÁS” en la parte inferior. Al pulsarlo,
 *    despliega un modal que explica: “Los hábitos en modo reto se identifican con un 🔥”,
 *    precedido de un ícono redondeado de fuego.
 *
 * @param vm La instancia de [HabitsViewModel] proporcionada por Hilt.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitsScreen(
    vm: HabitsViewModel = hiltViewModel()
) {
    val tutorialVm: com.app.tibibalance.tutorial.TutorialViewModel = hiltViewModel()
    // Estado local para controlar visibilidad del modal de “Agregar hábito”
    var showAdd by remember { mutableStateOf(false) }

    // Estado local que guarda el ID del hábito que se está editando (o null si no hay ninguno)
    var editingId by remember { mutableStateOf<HabitId?>(null) }

    // Estado local para controlar visibilidad del modal informativo “SABER MÁS”
    var showInfo by remember { mutableStateOf(false) }

    // Observamos el estado de la UI desde el ViewModel
    val ui by vm.uiState.collectAsState()

    val tutorialStep by tutorialVm.currentStep.collectAsState()
    val highlightFab = tutorialStep?.id == "habit_fab"

    // Recogemos eventos (SideEffects) enviados por el ViewModel
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                HabitsEvent.AddClicked -> {
                    // El usuario pulsó “Agregar”, abrimos el modal
                    showAdd = true
                }
                is HabitsEvent.ShowDetails -> {
                    // El usuario pulsó “Editar” en un hábito, guardamos su ID para mostrar el modal de edición
                    editingId = HabitId(event.habitId)
                }
            }
        }
    }

    // Contenedor principal: fondo degradado y contenido
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient())
    ) {
        androidx.compose.material3.IconButton(onClick = { tutorialVm.restartTutorial() }, modifier = Modifier.align(Alignment.TopEnd)) {
            androidx.compose.material3.Icon(Icons.Default.Help, contentDescription = "Ayuda")
        }
        // Mostrar contenido según el estado actual de la UI
        when (val state = ui) {
            HabitsUiState.Loading -> {
                // Mostrar indicador de carga
                Centered("Cargando…")
            }
            is HabitsUiState.Error -> {
                // Mostrar mensaje de error
                Centered(state.msg)
            }
            HabitsUiState.Empty -> {
                // No hay hábitos: invitación a agregar uno nuevo
                EmptyState(onAdd = vm::onAddClicked)
            }
            is HabitsUiState.Loaded -> {
                // Mostrar lista de hábitos
                HabitList(
                    habits = state.data,
                    onCheck = { habitId, checked ->
                        // TODO: gestionar marcación de hábito completado
                    },
                    onEdit = vm::onHabitClicked,
                    onAdd = vm::onAddClicked
                )
            }
        }

        // Enlace de texto “SABER MÁS” siempre visible,
        // alineado en la parte inferior centro con un padding
        TextButtonLink(
            text = "SABER MÁS",
            onClick = { showInfo = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                vm.onAddClicked()
                tutorialVm.proceedToNextStep()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (highlightFab) Color.Yellow else MaterialTheme.colorScheme.primary
            )
        ) {
            Text("+ Hábito")
        }

    }

    // Modal para agregar un nuevo hábito (si showAdd == true)
    if (showAdd) {
        AddHabitModal(onDismiss = { showAdd = false })
    }

    // Modal para editar un hábito existente (si editingId != null)
    editingId?.let { habitId ->
        EditHabitModal(
            habitId = habitId,
            onDismiss = { editingId = null }
        )
    }

    // Modal informativo “SABER MÁS” (si showInfo == true)
    if (showInfo) {
        ModalContainer(
            onDismissRequest = { showInfo = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono redondeado de fuego (flame) antes del texto
                Box(
                    modifier = Modifier
                        .size(72.dp) // Tamaño del contenedor circular
                        .clip(CircleShape) // Recorta en círculo
                        .background(MaterialTheme.colorScheme.primaryContainer), // Fondo circular
                    contentAlignment = Alignment.Center // Centrar el icono dentro del círculo
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Ícono de fuego",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp) // Tamaño del icono
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Texto explicativo sobre los hábitos en modo reto
                Text(
                    text = "Los hábitos en modo \n reto se identifican con un 🔥",
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

                // Botón para cerrar el modal
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
