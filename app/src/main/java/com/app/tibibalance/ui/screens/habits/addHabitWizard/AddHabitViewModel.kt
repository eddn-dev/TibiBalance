package com.app.tibibalance.ui.screens.habits.addHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.config.RepeatPreset
import com.app.domain.entities.Habit
import com.app.domain.model.HabitForm
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit
import com.app.domain.usecase.habit.CreateHabit
import com.app.domain.usecase.habit.GetSuggestedHabits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


import android.content.Context
import androidx.work.WorkManager
import com.app.domain.repository.AuthRepository
import com.app.domain.service.AlertManager
import com.app.domain.usecase.activity.GenerateActivitiesForHabit
import com.app.domain.usecase.habit.GetHabitsFlow
import com.app.domain.usecase.user.UnlockAchievementUseCase
import com.app.tibibalance.ui.common.validation.HabitFormValidator
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn


@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val createHabit : CreateHabit,
    getSuggested           : GetSuggestedHabits,
    private val unlockAchievement: UnlockAchievementUseCase,
    private val auth: AuthRepository,
    private val getHabitsFlow: GetHabitsFlow,
    private val genForHabit: GenerateActivitiesForHabit,
    private val alertMgr: AlertManager
) : ViewModel() {

    private val _ui = MutableStateFlow(AddHabitUiState())
    val ui: StateFlow<AddHabitUiState> = _ui.asStateFlow()

    val suggestions = getSuggested()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /* ---------- actualización del formulario ---------- */
    fun updateForm(newForm: HabitForm) = _ui.update {
        it.copy(form = newForm, hasChanges = true)
    }

    private fun resetState() { _ui.value = AddHabitUiState() }

    /* ---------- navegación ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun save() = viewModelScope.launch {
        /* 1️⃣  Spinner ON */
        _ui.update { it.copy(saving = true) }

        val uid   = auth.authState().firstOrNull()           // puede ser null
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
            if (uid != null) {
                launch(Dispatchers.IO) {                    // <- coroutine hija
                    runCatching { processAchievements(uid, habit) }
                }
            }

        } catch (ex: Exception) {
            _ui.update { it.copy(
                saving   = false,
                errorMsg = ex.message ?: "Error desconocido"
            ) }
        }
    }


    /**
     * Evalúa y desbloquea logros relacionados con la creación del [habit].
     * Se llama solo si existe un uid válido (es decir, cuando el usuario
     * está autenticado y la capa remota está disponible).
     */
    private suspend fun processAchievements(uid: String, habit: Habit) {
        /* ---------- logro por categoría ---------- */
        val catAchievement = when (habit.category.name.lowercase()) {
            "salud"         -> AchievementUnlocked(
                id = "tibio_salud",
                name = "Tibio saludable",
                description = "Agrega un hábito de salud."
            )
            "productividad" -> AchievementUnlocked(
                id = "tibio_productividad",
                name = "Tibio productivo",
                description = "Agrega un hábito de productividad."
            )
            "bienestar"     -> AchievementUnlocked(
                id = "tibio_bienestar",
                name = "Tibio del bienestar",
                description = "Agrega un hábito de bienestar."
            )
            else -> null
        }

        catAchievement?.let { logro ->
            if (unlockAchievement(uid, logro.id)) {
                pushAchievement(logro)
            }
        }

        /* ---------- logros por hábitos con modo reto ---------- */
        val retos = getHabitsFlow().first().filter { it.challenge != null }

        // progreso incremental para “cinco_habitos”
        val progreso = (retos.size.coerceAtMost(5) * 20)   // 1 reto = 20 %, … 5 retos = 100 %

        if (progreso < 100) {
            unlockAchievement.updateProgress(uid, "cinco_habitos", progreso)
        } else {
            val l3 = AchievementUnlocked(
                id = "cinco_habitos",
                name = "La sendera del reto",
                description = "Agrega cinco hábitos con modo reto activado."
            )
            if (unlockAchievement(uid, l3.id)) {
                pushAchievement(l3)
            }
        }

        // logro “primer_habito” (solo cuando el primer reto se agrega)
        if (retos.size == 1) {
            val l1 = AchievementUnlocked(
                id = "primer_habito",
                name = "El inicio del reto",
                description = "Agrega tu primer hábito con modo reto activado."
            )
            if (unlockAchievement(uid, l1.id)) {
                pushAchievement(l1)
            }
        }
    }


    /* ---------- validaciones ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun pushAchievement(logro: AchievementUnlocked) {
        _pendingAchievements.add(logro)
    }
}
