@file:Suppress("OPT_IN_IS_NOT_ENABLED") // Para kotlinx.datetime
package com.app.tibibalance.ui.screens.habits.addHabitWizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.HabitFormMappers.toHabit
import com.app.data.repository.IoDispatcher
import com.app.domain.entities.*
import com.app.domain.config.NotifConfig
import com.app.domain.error.BasicError
import com.app.domain.repository.HabitTemplateRepository
import com.app.domain.usecase.habit.*
import com.app.domain.usecase.template.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val getTemplates  : GetTemplatesFlowUseCase,
    private val refresh       : RefreshTemplatesUseCase,
    private val startSync     : StartTplSyncUseCase,
    private val createHabit   : CreateHabitUseCase,
    private val updateHabit   : UpdateHabitUseCase,
    private val syncHabits    : SyncHabitsUseCase,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {

    /* -------------- wizard page index --------------- */
    private val _page = MutableStateFlow(0)
    val page: StateFlow<Int> = _page.asStateFlow()

    /* -------------- template catalogue -------------- */
    val templates: StateFlow<List<HabitTemplate>> =
        getTemplates().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            refresh()
        }
        viewModelScope.launch {
            startSync()
        }
    }

    /* -------------- form state ---------------------- */
    private val _form = MutableStateFlow(HabitForm())
    val form: StateFlow<HabitForm> = _form.asStateFlow()

    /* -------------- notif config -------------------- */
    private val _notif = MutableStateFlow(NotifConfig())
    val notif: StateFlow<NotifConfig> = _notif.asStateFlow()

    /* -------------- UI-side effects ----------------- */
    private val _events = MutableSharedFlow<WizardEvent>()
    val events = _events.asSharedFlow()

    /* ----- template selection ---- */
    fun applyTemplate(tpl: HabitTemplate) {
        _form.value = tpl.formDraft.copy()
        _page.value = 1                           // jump to BasicInfo
    }

    /* ----- navigation helpers ---- */
    fun next() { _page.update { minOf(it + 1, 3) } }
    fun back() { _page.update { maxOf(it - 1, 0) } }

    /* ----- two-way bindings from Composables ---- */
    fun updateBasic(f: HabitForm)  { _form.value = f }
    fun updateTrack(f: HabitForm)  { _form.value = f }
    fun updateNotif(n: NotifConfig){ _notif.value = n }

    /* ----- validation checks per step ---- */
    fun basicErrors(f: HabitForm): List<BasicError> =
        if (f.name.isBlank()) listOf(BasicError.NameRequired) else emptyList()

    /* ----- save habit ---- */
    fun save() = viewModelScope.launch {
        val habit = withContext(io) { _form.value.toHabit(_notif.value) }
        val result = createHabit(habit)

        if (result.isSuccess) {
            _events.emit(WizardEvent.Saved)
            syncHabits()
        } else {
            _events.emit(
                WizardEvent.Error(result.exceptionOrNull()?.localizedMessage ?: "Error desconocido")
            )
        }
    }
}
