package com.app.tibibalance.ui.screens.habits

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.ids.HabitId
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.EmptyState
import com.app.tibibalance.ui.components.utils.HabitList
import com.app.tibibalance.ui.screens.habits.addHabitWizard.AddHabitModal
import com.app.tibibalance.ui.screens.habits.editHabitWizard.EditHabitModal

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitsScreen(
    vm: HabitsViewModel = hiltViewModel()
) {
    var showAdd by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<HabitId?>(null) }

    val ui by vm.uiState.collectAsState()
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                HabitsEvent.AddClicked     -> showAdd = true
                is HabitsEvent.ShowDetails -> editingId = HabitId(ev.habitId)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(gradient)
    ) {
        when (val s = ui) {
            HabitsUiState.Loading -> Centered("Cargando…")
            is HabitsUiState.Error -> Centered(s.msg)
            HabitsUiState.Empty    -> EmptyState(onAdd = vm::onAddClicked)
            is HabitsUiState.Loaded -> HabitList(
                habits  = s.data,
                onCheck = { _, _ -> /* TBD */ },
                onEdit  = vm::onHabitClicked,
                onAdd   = vm::onAddClicked
            )
        }
    }

    if (showAdd) {
        AddHabitModal(onDismiss = { showAdd = false })
    }

    editingId?.let {
        EditHabitModal(habitId = it, onDismiss = { editingId = null })
    }
}
