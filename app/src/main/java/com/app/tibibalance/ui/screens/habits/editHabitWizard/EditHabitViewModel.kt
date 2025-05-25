/* ui/screen/habits/editHabitWizard/EditHabitViewModel.kt */
package com.app.tibibalance.ui.screens.habits.editHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.config.ChallengeConfig
import com.app.domain.config.RepeatPreset
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit
import com.app.domain.ids.HabitId
import com.app.domain.model.HabitForm
import com.app.domain.usecase.habit.DeleteHabit
import com.app.domain.usecase.habit.GetHabitById
import com.app.domain.usecase.habit.UpdateHabit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val getHabit   : GetHabitById,
    private val updateHabit: UpdateHabit,
    private val deleteHabit: DeleteHabit
) : ViewModel() {

    /* ───────── estado interno ───────── */
    private val habitId = MutableStateFlow<HabitId?>(null)

    /** Flujo reactivo del hábito seleccionado */
    @OptIn(ExperimentalCoroutinesApi::class)
    val habit: StateFlow<com.app.domain.entities.Habit?> =
        habitId.filterNotNull()
            .flatMapLatest { getHabit(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val challengeActive: StateFlow<Boolean> =
            habit.map { it?.challenge != null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /* --- UI-state --- */
    private val _ui = MutableStateFlow(EditHabitUiState(showOnly = true))
    val ui: StateFlow<EditHabitUiState> = _ui.asStateFlow()

    /* --- eventos one-shot --- */
    private val _events = Channel<EditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /* ---------- API pública ---------- */

    fun load(id: HabitId) {
        habitId.value = id

        // Estado limpio: vista-solo, paso 0, sin datos previos
        _ui.value = EditHabitUiState(
            showOnly    = true,
            currentStep = 0,
            form        = HabitForm()
        )
    }


    fun startEditing() = _ui.update { st ->
        val pre = habit.value?.toForm() ?: st.form
        if (challengeActive.value) {                     // reto ⇒ sólo notif
            st.copy(showOnly = false, currentStep = 3,   // 3 existe siempre
                backToShowOnlyFrom3 = true, form = pre)
        } else {
            st.copy(showOnly = false, currentStep = 1, form = pre)
        }
    }

    fun jumpToNotif()   = _ui.update { st->
        val prefilled = habit.value?.toForm() ?: st.form
        st.copy(showOnly = false, currentStep = 3, backToShowOnlyFrom3 = true, form = prefilled)
    }

    fun updateForm(f: HabitForm) = _ui.update { it.copy(form = f, hasChanges = true) }
    /* EditHabitViewModel.kt  – sólo se muestran los métodos editados */

    fun back() = _ui.update { st ->
        when {
            /* Paso 3 -> 0 proveniente del resumen  */
            st.currentStep == 3 && st.backToShowOnlyFrom3 ->
                st.copy(
                    showOnly            = true,
                    currentStep         = 0,
                    backToShowOnlyFrom3 = false,
                    hasChanges          = false        // no queremos advertir «salir sin guardar»
                )

            /* Paso 1 ó 2 -> 0 (no-reto)                                    */
            st.currentStep == 1 && !challengeActive.value -> st.copy(
                showOnly    = true,
                currentStep = 0,
                hasChanges  = false                       // cancelamos edición
            )
            st.currentStep == 2 && !challengeActive.value -> st.copy(
                showOnly    = true,
                currentStep = 0,
                hasChanges  = false
            )

            /* flujo normal */
            else -> st.copy(currentStep = (st.currentStep - 1).coerceAtLeast(0))
        }
    }

    fun next() = _ui.update { st ->
        if (!challengeActive.value)
            st.copy(currentStep = (st.currentStep + 1).coerceAtMost(3))
        else st
    }

    fun save() = viewModelScope.launch {
        val id       = habitId.value ?: return@launch
        val original = habit.value   ?: return@launch          // ①

        _ui.update { it.copy(saving = true) }

        // ②  Generamos el nuevo objeto *preservando* createdAt
        val updated = ui.value.form
            .toHabit(id, now = Clock.System.now())
            .copy(meta = original.meta.copy(   // ← sólo cambiamos lo permitido
                updatedAt   = Clock.System.now(),
                pendingSync = true
            ))

        runCatching { updateHabit(updated) }
            .onSuccess { _ui.value = EditHabitUiState(savedOk = true) }
            .onFailure  { ex ->
                _ui.update { it.copy(saving = false, errorMsg = ex.message) }
            }
    }

    fun delete() = viewModelScope.launch {
        habitId.value?.let { deleteHabit(it) }
        _events.send(EditEvent.Dismiss)
    }

    /* --- validaciones idénticas al wizard de alta --- */
    fun isStepValid(step: Int, f: HabitForm): Boolean = when (step) {
        1 -> f.name.isNotBlank()
        2 -> when {
            f.challenge &&
                    (f.periodUnit == PeriodUnit.INDEFINIDO || f.periodQty == null) -> false
            f.repeatPreset == RepeatPreset.PERSONALIZADO && f.weekDays.isEmpty()   -> false
            f.periodUnit != PeriodUnit.INDEFINIDO && f.periodQty == null          -> false
            f.sessionUnit != SessionUnit.INDEFINIDO && f.sessionQty == null       -> false
            else -> true
        }
        3 -> !f.notify || f.notifTimes.isNotEmpty()
        else -> true
    }

    fun acknowledgeSaved() = viewModelScope.launch { _events.send(EditEvent.Dismiss) }

    // EditHabitViewModel.kt
    fun toggleNotifications(enabled: Boolean) = viewModelScope.launch {
        habit.value?.let { current ->
            updateHabit(current.copy(notifConfig = current.notifConfig.copy(enabled = enabled)))
        }
    }

    fun requestExit() {
        val st = _ui.value
        if (st.showOnly || (!st.hasChanges && !st.saving)) {
            _events.trySend(EditEvent.Dismiss)     // nada que perder
        } else {
            _ui.update { it.copy(askExit = true) } // pedir confirmación
        }
    }

    fun confirmExit(confirm:Boolean) {
        if (confirm) _events.trySend(EditEvent.Dismiss)
        _ui.update { it.copy(askExit = false) }
    }

    /* --- sincroniza cuando cambia el hábito --- */
    init {
        viewModelScope.launch {
            habit.filterNotNull().collect { h ->
                _ui.update { it.copy(form = h.toForm()) }
            }
        }
    }
}
