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
        index++
        if (index >= steps.size) {
            finishTutorial()
        } else {
            _current.value = steps[index]
        }
    }

    fun skipTutorial() { finishTutorial() }

    fun restartTutorial() {
        index = 0
        _current.value = steps.first()
    }

    private fun finishTutorial() {
        viewModelScope.launch {
            val uid = uidProvider()
            saveTutorialStatus(uid, true)
            _current.value = null
        }
    }
}
