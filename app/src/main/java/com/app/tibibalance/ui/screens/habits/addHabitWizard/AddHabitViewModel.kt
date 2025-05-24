package com.app.tibibalance.ui.screens.habits.addHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.entities.Habit
import com.app.domain.entities.HabitForm
import com.app.domain.usecase.habit.CreateHabit
import com.app.domain.usecase.habit.GetSuggestedHabits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val createHabit : CreateHabit,
    getSuggested           : GetSuggestedHabits
) : ViewModel() {

    private val _ui = MutableStateFlow(AddHabitUiState())
    val ui: StateFlow<AddHabitUiState> = _ui.asStateFlow()

    val suggestions = getSuggested()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /* ---------- actualización del formulario ---------- */
    fun updateForm(update: (HabitForm) -> HabitForm) = _ui.update {
        it.copy(form = update(it.form), hasChanges = true)
    }

    /* ---------- navegación ---------- */
    fun next() = _ui.update {
        if (isStepValid(it.currentStep, it.form))
            it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3))
        else it
    }
    fun back()  = _ui.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }

    /* ---------- cierre del modal ---------- */
    fun requestExit() = _ui.update {
        if (it.hasChanges && !it.saving) it.copy(askExit = true) else it
    }
    fun confirmExit(confirm: Boolean) {
        if (confirm) _events.trySend(WizardEvent.Dismiss)
        _ui.update { it.copy(askExit = false) }
    }

    /* ---------- plantillas ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
    fun pickSuggestion(habit: Habit) = _ui.update {
        if (it.hasChanges) it.copy(askReplace = true, tempTpl = habit)
        else it.applyTemplate(habit)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmReplace(confirm: Boolean) = _ui.update {
        if (confirm && it.tempTpl != null) it.applyTemplate(it.tempTpl!!)
        else it.copy(askReplace = false, tempTpl = null)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun AddHabitUiState.applyTemplate(h: Habit) = copy(
        askReplace = false,
        tempTpl    = null,
        form       = h.toForm(),
        hasChanges = true,
        currentStep = 1
    )

    /* ---------- guardado ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
    fun save() = viewModelScope.launch {
        val state = _ui.updateAndGet { it.copy(saving = true) }
        runCatching { createHabit(state.form.toHabit()) }
            .onSuccess { _ui.value = AddHabitUiState(savedOk = true) }     // reset + éxito
            .onFailure { ex -> _ui.update { it.copy(saving = false, errorMsg = ex.message) } }
    }

    /* ---------- validaciones ---------- */
    fun isStepValid(step: Int, f: HabitForm): Boolean = when (step) {
        1 -> f.name.isNotBlank()
        2 -> true
        3 -> !f.notify || f.notifTimes.isNotEmpty()
        else -> true
    }

    /* ---------- canal eventos ---------- */
    private val _events = Channel<WizardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
}
