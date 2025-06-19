package com.app.tibibalance.ui.screens.habits.addHabitWizard


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.achievements.event.AchievementEvent
import com.app.domain.entities.Achievement
import com.app.domain.entities.Habit
import com.app.domain.model.HabitForm
import com.app.domain.repository.AuthRepository
import com.app.domain.service.AlertManager
import com.app.domain.usecase.achievement.CheckUnlockAchievement
import com.app.domain.usecase.activity.GenerateActivitiesForHabit
import com.app.domain.usecase.activity.ObserveHabitActivities
import com.app.domain.usecase.habit.CreateHabit
import com.app.domain.usecase.habit.GetHabitsFlow
import com.app.domain.usecase.habit.GetSuggestedHabits
import com.app.tibibalance.ui.common.validation.HabitFormValidator
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject


@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val createHabit : CreateHabit,
    getSuggested           : GetSuggestedHabits,
    private val checkAchievement: CheckUnlockAchievement,
    private val activities : ObserveHabitActivities,
    private val genForHabit: GenerateActivitiesForHabit,
    private val alertMgr: AlertManager
) : ViewModel() {

    private val _ui = MutableStateFlow(AddHabitUiState())
    val ui: StateFlow<AddHabitUiState> = _ui.asStateFlow()

    private val _unlocked = MutableSharedFlow<AchievementUnlocked>(extraBufferCapacity = 1)
    val unlocked: SharedFlow<AchievementUnlocked> = _unlocked

    val suggestions = getSuggested()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /* ---------- actualización del formulario ---------- */
    fun updateForm(newForm: HabitForm) = _ui.update {
        it.copy(form = newForm, hasChanges = true)
    }

    private fun resetState() { _ui.value = AddHabitUiState() }

    /* ---------- navegación ---------- */
    fun next() = _ui.update {
        if (isStepValid(it.currentStep, it.form))
            it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3))
        else it
    }
    fun back()  = _ui.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }

    /* ---------- cierre del modal ---------- */
    fun requestExit() {
        val state = _ui.value
        if (state.hasChanges || state.saving) {
            /* hay cambios → pedimos confirmación */
            _ui.update { it.copy(askExit = true) }
        } else {
            /* limpio → cerrar de inmediato */
            _events.trySend(WizardEvent.Dismiss)
        }
    }


    fun confirmExit(confirm: Boolean) {
        if (confirm) {
            resetState()
            _events.trySend(WizardEvent.Dismiss)
        }
        _ui.update { it.copy(askExit = false) }
    }

    /* ---------- plantillas ---------- */
    fun pickSuggestion(habit: Habit) = _ui.update {
        if (it.hasChanges) it.copy(askReplace = true, tempTpl = habit)
        else it.applyTemplate(habit)
    }
    fun confirmReplace(confirm: Boolean) = _ui.update {
        if (confirm && it.tempTpl != null) it.applyTemplate(it.tempTpl!!)
        else it.copy(askReplace = false, tempTpl = null)
    }
    private fun AddHabitUiState.applyTemplate(h: Habit) = copy(
        askReplace = false,
        tempTpl    = null,
        form       = h.toForm(),
        hasChanges = true,
        currentStep = 1
    )

    fun save() = viewModelScope.launch {
        /* 1️⃣  Spinner ON */
        _ui.update { it.copy(saving = true) }

        val habit = _ui.value.form.toHabit()

        try {
            /* 2️⃣  ROOM: siempre */
            createHabit(habit)

            /* 3️⃣  Actividades de reto (si procede) */
            if (habit.challenge != null) {
                withContext(Dispatchers.IO) {
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    genForHabit(habit, today)
                }
            }
            alertMgr.schedule(habit)

            /* 4️⃣  UI de éxito INMEDIATA */
            _ui.update { it.copy(
                saving  = false,
                savedOk = true
            ) }

            /* 5️⃣  Logros en segundo plano (no bloquea) */
            launch(Dispatchers.IO) {
                checkAchievement(
                    AchievementEvent.HabitAdded(
                        category    = habit.category,
                        isChallenge = habit.challenge != null
                    )
                ).forEach { ach ->
                    val uiAch = ach.toUi()
                    _pendingAchievements += uiAch
                    _unlocked.emit(uiAch)
                }
            }

            // Obtener las actividades de hoy
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val activities = activities(habit.id).firstOrNull()
            Log.w("ActividadesGeneradas", "Actividades generadas: $activities para el hábito ${habit.name} el día $today")

        } catch (ex: Exception) {
            _ui.update { it.copy(
                saving   = false,
                errorMsg = ex.message ?: "Error desconocido"
            ) }
        }
    }

    /* ---------- validaciones ---------- */
    fun isStepValid(step: Int, f: HabitForm): Boolean =
        HabitFormValidator.isStepValid(step, f)

    fun acknowledgeSaved() {
        resetState()                                // ← vuelve todo a paso 0
        _events.trySend(WizardEvent.Dismiss)        // ← cierra el modal
    }

    fun consumeSaved() {
        _ui.update { it.copy(savedOk = false) }
    }

    /* ---------- canal eventos ---------- */
    private val _events = Channel<WizardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _pendingAchievements = mutableListOf<AchievementUnlocked>()

    fun popNextAchievement(): AchievementUnlocked? =
        _pendingAchievements.removeFirstOrNull()


    private fun Achievement.toUi() =
        AchievementUnlocked(id.raw, name, description)
}