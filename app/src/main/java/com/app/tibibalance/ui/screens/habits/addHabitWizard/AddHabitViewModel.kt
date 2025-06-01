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
import com.app.domain.usecase.habit.GetHabitsFlow
import com.app.domain.usecase.user.UnlockAchievementUseCase
import com.app.tibibalance.sync.EvaluarLogrosWorker
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked


@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val createHabit : CreateHabit,
    getSuggested           : GetSuggestedHabits,
    private val unlockAchievement: UnlockAchievementUseCase,
    private val auth: AuthRepository,
    private val getHabitsFlow: GetHabitsFlow
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

    /* ---------- guardado ---------- */
    /*@RequiresApi(Build.VERSION_CODES.O)
    fun save() = viewModelScope.launch {
        val state = _ui.updateAndGet { it.copy(saving = true) }
        runCatching { createHabit(state.form.toHabit()) }
            .onSuccess {
                /* mostramos diálogo de éxito */
                _ui.value = AddHabitUiState(savedOk = true)

            }
            .onFailure { ex ->
                _ui.update { it.copy(saving = false, errorMsg = ex.message) }
            }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(context: Context) = viewModelScope.launch {
        val uid = auth.authState().first() ?: return@launch
        val state = _ui.updateAndGet { it.copy(saving = true) }

        runCatching {
            val habit = state.form.toHabit()
            createHabit(habit)

            // --- Lógica de desbloqueo según categoría ---
            val logro = when (habit.category.name.lowercase()) {
                "salud" -> AchievementUnlocked(
                    id = "tibio_salud",
                    name = "Tibio saludable",
                    description = "Agrega un hábito de salud."
                )
                "productividad" -> AchievementUnlocked(
                    id = "tibio_productividad",
                    name = "Tibio productivo",
                    description = "Agrega un hábito de productividad."
                )
                "bienestar" -> AchievementUnlocked(
                    id = "tibio_bienestar",
                    name = "Tibio del bienestar",
                    description = "Agrega un hábito de bienestar."
                )
                else -> null
            }

            logro?.let {
                if (unlockAchievement(uid, it.id)) {
                    pushAchievement(it)
                }
            }

            // Verifica logros por hábitos con modo reto activado
            // Verifica logros por hábitos con modo reto activado
            getHabitsFlow().first()
                .filter { it.challenge != null }
                .let { retos ->

                    // Calcula progreso (mínimo 1 reto = 33%, máximo 3 retos = 100%)
                    val progreso = (retos.size.coerceAtMost(5) * 20)

                    // Si aún no llega al 100%, solo actualiza progreso
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

                    // Manejo independiente del de "primer_habito"
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

            // Lanza Worker (si sigue siendo necesario)
            val work = EvaluarLogrosWorker.oneTime(uid)
            WorkManager.getInstance(context).enqueue(work)

            // Notifica éxito
            _ui.value = AddHabitUiState(savedOk = true)
        }.onFailure { ex ->
            _ui.update { it.copy(saving = false, errorMsg = ex.message) }
        }
        _ui.value = AddHabitUiState(savedOk = true)
    }



    /* ---------- validaciones ---------- */
    fun isStepValid(step: Int, f: HabitForm): Boolean = when (step) {
        /* Paso 1 – nombre obligatorio */
        1 -> f.name.isNotBlank()

        /* Paso 2 – seguimiento */
        2 -> when {
            /* reto exige periodo definido */
            f.challenge &&
                    (f.periodUnit == PeriodUnit.INDEFINIDO || f.periodQty == null)          -> false

            /* repetición personalizada exige días */
            f.repeatPreset == RepeatPreset.PERSONALIZADO && f.weekDays.isEmpty()        -> false

            /* si el usuario eligió unidad de periodo, debe poner cantidad */
            f.periodUnit != PeriodUnit.INDEFINIDO && f.periodQty == null               -> false

            /* si eligió unidad de sesión, debe poner cantidad */
            f.sessionUnit != SessionUnit.INDEFINIDO && f.sessionQty == null            -> false

            else -> true
        }

        /* Paso 3 – si “notificar” está activo, al menos una hora */
        3 -> !f.notify || f.notifTimes.isNotEmpty()

        else -> true
    }

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
