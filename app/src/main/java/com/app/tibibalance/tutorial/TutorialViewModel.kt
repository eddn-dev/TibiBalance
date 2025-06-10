package com.app.tibibalance.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.repository.HabitRepository
import com.app.domain.usecase.onboarding.ObserveOnboardingStatus
import com.app.domain.usecase.tutorial.SaveTutorialStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** ViewModel that controls the tutorial flow. */
@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val observeStatus: ObserveOnboardingStatus,
    private val saveTutorialStatus: SaveTutorialStatusUseCase,
    private val habitRepo: HabitRepository,
    private val uidProvider: AuthUidProvider
) : ViewModel() {

    private val steps = TutorialSteps.all
    private var index = 0

    private val _current = MutableStateFlow<TutorialStepData?>(null)
    val currentStep: StateFlow<TutorialStepData?> = _current

    init { viewModelScope.launch { startIfNeeded() } }

    private suspend fun startIfNeeded() {
        val uid = uidProvider()
        val status = observeStatus(uid).first()
        if (status.tutorialCompleted && !status.hasCompletedTutorial) {
            _current.value = steps.first()
        }
    }

    fun proceedToNextStep() {
        val index = TutorialSteps.all.indexOf(_current.value)
        if (index == -1 || index + 1 >= TutorialSteps.all.size) {
            finishTutorial()
            return
        }

        val nextStep = TutorialSteps.all[index + 1]

        if (_current.value?.id == "habit_fab" && !hasRetoHabit()) {
            _current.value = TutorialSteps.all.firstOrNull { it.id == "daily_tip" }
        } else if ((nextStep.id == "daily_progress" || nextStep.id == "activity_fab") && !hasRetoHabit()) {
            _current.value = TutorialSteps.all.firstOrNull { it.id == "daily_tip" }
        } else {
            _current.value = nextStep
        }
    }

    fun skipTutorial() { finishTutorial() }

    fun restartTutorial() {
        index = 0
        _current.value = steps.first()
    }

    internal fun finishTutorial() {
        viewModelScope.launch {
            val uid = uidProvider()
            saveTutorialStatus(uid, true)
            _current.value = null
        }
    }

    var habitFabSkipped = false
        private set

    fun skipHabitFab() {
        habitFabSkipped = true
        proceedToNextStep()
    }

    private fun hasRetoHabit(): Boolean {
        // TODO: reemplaza con acceso real a ViewModel o repositorio si aplica
        return true // <-- o consulta vm.habits.any { it.isReto && it.hasQuantity }
    }
}
