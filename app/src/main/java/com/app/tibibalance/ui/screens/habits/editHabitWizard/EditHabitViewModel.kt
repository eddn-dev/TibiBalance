/* ui/screen/habits/editHabitWizard/EditHabitViewModel.kt */
package com.app.tibibalance.ui.screens.habits.editHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.ids.HabitId
import com.app.domain.model.HabitForm
import com.app.domain.service.AlertManager
import com.app.domain.usecase.habit.DeleteHabit
import com.app.domain.usecase.habit.GetHabitById
import com.app.domain.usecase.habit.UpdateHabit
import com.app.tibibalance.ui.common.validation.HabitFormValidator
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
    private val deleteHabit: DeleteHabit,
    private val alertMgr   : AlertManager
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
        _ui.value = EditHabitUiState(showOnly = true)

        viewModelScope.launch {
            getHabit(id).first()?.let {        // primer valor no nulo
                _ui.update { it.copy(form = it.form) }
            } ?: _events.send(EditEvent.Dismiss)   // ⚠️  desapareció → cerrar
        }
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

    /* ─────────── SAVE (UPDATE) ─────────── */

    fun save() = viewModelScope.launch {
        val id       = habitId.value ?: return@launch
        val original = habit.value   ?: return@launch

        _ui.update { it.copy(saving = true) }

        val updated = ui.value.form
            .toHabit(id, now = Clock.System.now())
            .copy(meta = original.meta.copy(
                updatedAt   = Clock.System.now(),
                pendingSync = true
            ))

        runCatching { updateHabit(updated) }
            .onSuccess {
                /* ①  refresca alarmas */
                alertMgr.cancel(id)
                if (updated.notifConfig.enabled) {
                    alertMgr.schedule(updated)
                }

                _ui.value = EditHabitUiState(savedOk = true)
            }
            .onFailure { ex ->
                _ui.update { it.copy(saving = false, errorMsg = ex.message) }
            }
    }

    fun requestDelete() = _ui.update { it.copy(askDelete = true) }

    /**
     * El usuario respondió al diálogo de confirmación.
     * - Si acepta ⇒ ejecutamos `delete()`.
     * - En ambos casos ocultamos el diálogo.
     */
    fun confirmDelete(confirm: Boolean) {
        if (confirm) delete()
        _ui.update { it.copy(askDelete = false) }
    }

    /* ─────────── DELETE ─────────── */

    private fun delete() = viewModelScope.launch {
        val id = habitId.value ?: return@launch
        _ui.update { it.copy(deleting = true) }

        runCatching { deleteHabit(id) }
            .onSuccess {
                /* ①  anula alarmas del hábito borrado */
                alertMgr.cancel(id)

                _ui.value = EditHabitUiState(deletedOk = true)
                _events.send(EditEvent.Dismiss)
            }
            .onFailure { ex ->
                _ui.update { it.copy(deleting = false, errorMsg = ex.message) }
            }
    }

    /* --- validaciones idénticas al wizard de alta --- */
    fun isStepValid(step: Int, f: HabitForm): Boolean =
        HabitFormValidator.isStepValid(step, f)

    fun acknowledgeSaved() = viewModelScope.launch { _events.send(EditEvent.Dismiss) }

    // EditHabitViewModel.kt
    fun toggleNotifications(enabled: Boolean) = viewModelScope.launch {
        habit.value?.let { current ->
            val updated = current.copy(
                notifConfig = current.notifConfig.copy(enabled = enabled),
                meta        = current.meta.copy(
                    updatedAt   = Clock.System.now(),
                    pendingSync = true
                )
            )
            updateHabit(updated)

            /* ①  refresca alarmas en-caliente */
            alertMgr.cancel(current.id)
            if (enabled) alertMgr.schedule(updated)
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
