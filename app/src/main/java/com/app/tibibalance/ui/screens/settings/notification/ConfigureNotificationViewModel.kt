/* ui/screens/settings/notification/ConfigureNotificationViewModel.kt */
package com.app.tibibalance.ui.screens.settings.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.usecase.habit.GetHabitById
import com.app.domain.usecase.habit.GetHabitsFlow
import com.app.domain.usecase.habit.UpdateHabit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

/* ───────────── modelos UI ───────────── */

data class HabitNotifUi(
    val id      : String,
    val name    : String,
    val icon    : String,
    val enabled : Boolean
)

sealed class ConfigureNotifUiState {
    object Loading                       : ConfigureNotifUiState()
    object Empty                         : ConfigureNotifUiState()
    data class Loaded(val data: List<HabitNotifUi>) : ConfigureNotifUiState()
    data class Error (val msg : String)  : ConfigureNotifUiState()
}

/* ───────────── ViewModel ───────────── */

@HiltViewModel
class ConfigureNotificationViewModel @Inject constructor(
    getHabitsFlow : GetHabitsFlow,
    private val getHabitById : GetHabitById,
    private val updateHabit  : UpdateHabit
) : ViewModel() {

    /* ---------- listado reactivo ---------- */
    val ui: StateFlow<ConfigureNotifUiState> = getHabitsFlow()
        .map { list ->
            val items = list.map { it.toUi() }
            if (items.isEmpty()) ConfigureNotifUiState.Empty
            else                 ConfigureNotifUiState.Loaded(items)
        }
        .catch { emit(ConfigureNotifUiState.Error(it.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            ConfigureNotifUiState.Loading)

    /* ---------- hábito seleccionado para el modal ---------- */
    private val _selectedHabit = MutableStateFlow<HabitNotifUi?>(null)
    val selectedHabit: StateFlow<HabitNotifUi?> = _selectedHabit.asStateFlow()

    fun selectHabit(habit: HabitNotifUi) { _selectedHabit.value = habit }
    fun clearSelectedHabit()             { _selectedHabit.value = null }

    /* ---------- habilitar / deshabilitar campana ---------- */
    fun toggleNotification(habitUi: HabitNotifUi) = viewModelScope.launch {
        val habit = getHabitById(HabitId(habitUi.id)).first() ?: return@launch

        val updated = habit.copy(
            notifConfig = habit.notifConfig.copy(enabled = !habit.notifConfig.enabled),
            meta        = habit.meta.copy(
                updatedAt   = Clock.System.now(),
                pendingSync = true
            )
        )
        updateHabit(updated)
    }

    /* ---------- mapper dominio ➜ UI ---------- */
    private fun Habit.toUi() = HabitNotifUi(
        id      = id.value,
        name    = name,
        icon    = icon,
        enabled = notifConfig.enabled
    )
}
