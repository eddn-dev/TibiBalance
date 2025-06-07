package com.app.tibibalance.ui.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.repository.AuthRepository
import com.app.domain.repository.HabitRepository
import com.app.domain.usecase.onboarding.ObserveOnboardingStatus
import com.app.domain.usecase.tutorial.SaveTutorialStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val observeStatus: ObserveOnboardingStatus,
    private val saveStatus: SaveTutorialStatusUseCase,
    private val habitRepo: HabitRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow<TutorialStepData?>(null)
    val currentStep: StateFlow<TutorialStepData?> = _currentStep

    private val steps = TutorialStep.all(::hasChallengeHabit)
    private var index = -1

    init { startIfNeeded() }

    private fun startIfNeeded() {
        viewModelScope.launch {
            val uid = authRepo.authState().first() ?: return@launch
            val status = observeStatus(uid).first()
            if (status.tutorialCompleted && !status.hasCompletedTutorial) {
                proceedToNextStep()
            }
        }
    }

    fun proceedToNextStep() {
        viewModelScope.launch {
            do {
                index++
                if (index >= steps.size) { finishTutorial(); return@launch }
                val step = steps[index]
                val show = step.conditionalCheck?.invoke() ?: true
            } while (!show)
            _currentStep.value = steps[index]
        }
    }

    fun skipTutorial() {
        viewModelScope.launch {
            finishTutorial()
        }
    }

    fun restartTutorial() {
        index = -1
        proceedToNextStep()
    }

    private suspend fun hasChallengeHabit(): Boolean {
        return habitRepo.observeUserHabits().first().any { it.challenge != null }
    }

    private suspend fun finishTutorial() {
        _currentStep.value = null
        val uid = authRepo.authState().first() ?: return
        saveStatus(uid, true)
    }
}
