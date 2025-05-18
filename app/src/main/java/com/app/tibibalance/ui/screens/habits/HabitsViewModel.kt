/**
 * @file    HabitsViewModel.kt
 * @ingroup ui_screens_habits
 */
package com.app.tibibalance.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.usecase.habit.GetHabitsFlowUseCase
import com.app.tibibalance.ui.screens.habits.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    getHabits: GetHabitsFlowUseCase
) : ViewModel() {

    /* ---------- eventos one-shot ---------- */
    private val _events = Channel<HabitsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /* ---------- UI-state ---------- */
    val uiState: StateFlow<HabitsUiState> = getHabits()      // flow del caso de uso
        .map { list ->
            val items = list.map { it.toUi() }
            if (items.isEmpty()) HabitsUiState.Empty else HabitsUiState.Loaded(items)
        }
        .catch { emit(HabitsUiState.Error(it.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState.Loading)

    /* ---------- callbacks UI ---------- */
    fun onAddClicked() = viewModelScope.launch { _events.send(HabitsEvent.AddClicked) }
    fun onHabitClicked(habit: HabitUi) =
        viewModelScope.launch { _events.send(HabitsEvent.ShowDetails(habit.id)) }
}
