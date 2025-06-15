package com.app.tibibalance.tutorial

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.OnboardingStatus
import com.app.domain.repository.HabitRepository
import com.app.domain.usecase.onboarding.ObserveOnboardingStatus
import com.app.domain.usecase.tutorial.SaveTutorialStatusUseCase
import com.app.tibibalance.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.app.domain.repository.OnboardingRepository
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect


@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val observeStatus: ObserveOnboardingStatus,
    private val saveTutorialStatus: SaveTutorialStatusUseCase,
    private val habitRepo: HabitRepository,
    private val uidProvider: AuthUidProvider,
    private val onboardingRepo: OnboardingRepository
) : ViewModel() {

    enum class HomeTutorialSection {
        Main, Stats
    }

    private val _currentStep = MutableStateFlow<TutorialStepData?>(null)
    val currentStep: StateFlow<TutorialStepData?> = _currentStep

    private var currentScreen: Screen? = null
    private var steps: List<TutorialStepData> = emptyList()
    private var stepIndex = 0
    private var currentHomeSection: HomeTutorialSection? = null

    private val _targetBounds = mutableStateOf<Rect?>(null)
    val targetBounds: State<Rect?> = _targetBounds

    // ---------- dentro del ViewModel ----------

    private val _highlight = MutableStateFlow(Rect.Zero)   // Flow, no State
    val highlight: StateFlow<Rect> = _highlight

    fun updateTargetBounds(rect: Rect) {
        _highlight.value = rect                 // emite SIEMPRE el nuevo rect
        Log.d("Tuto-VM", "highlight=$rect")     // ← mira esto en Logcat
    }

    fun startHomeTutorialIfNeeded(section: HomeTutorialSection) {
        viewModelScope.launch {
            currentScreen = Screen.Home
            currentHomeSection = section
            val uid = uidProvider()
            val status = observeStatus(uid).first()

            val alreadySeen = when (section) {
                HomeTutorialSection.Main -> status.hasSeenTutorial_HomeScreenMain
                HomeTutorialSection.Stats -> status.hasSeenTutorial_HomeScreenStats
            }

            Log.d("TutorialVM", "startHomeTutorialIfNeeded($section) - alreadySeen = $alreadySeen")

            if (!alreadySeen) {
                steps = when (section) {
                    HomeTutorialSection.Main -> TutorialSteps.home
                    HomeTutorialSection.Stats -> TutorialSteps.stats
                }
                stepIndex = 0
                Log.d("TutorialVM", "Starting tutorial for $section, stepCount=${steps.size}")
                _currentStep.value = steps.firstOrNull()
            }
        }
    }

    fun restartHomeTutorial(section: HomeTutorialSection) {
        currentScreen = Screen.Home
        currentHomeSection = section
        steps = when (section) {
            HomeTutorialSection.Main -> TutorialSteps.home
            HomeTutorialSection.Stats -> TutorialSteps.stats
        }
        stepIndex = 0
        _currentStep.value = steps.firstOrNull()
    }

    fun startTutorialIfNeeded(screen: Screen) {
        viewModelScope.launch {
            currentScreen = screen
            val uid = uidProvider()
            val status = observeStatus(uid).first()

            val alreadySeen = when (screen) {
                Screen.Habits -> status.hasSeenTutorial_HabitsScreen
                Screen.Emotions -> status.hasSeenTutorial_EmotionsScreen
                Screen.Settings -> status.hasSeenTutorial_SettingsScreen
                else -> true
            }

            Log.d("TutorialVM", "startTutorialIfNeeded($screen) - alreadySeen = $alreadySeen")

            if (!alreadySeen) {
                steps = when (screen) {
                    Screen.Habits -> TutorialSteps.habits
                    Screen.Emotions -> TutorialSteps.emotions
                    Screen.Settings -> TutorialSteps.settings
                    else -> emptyList()
                }
                stepIndex = 0
                Log.d("TutorialVM", "Starting tutorial for $screen, stepCount=${steps.size}")
                _currentStep.value = steps.firstOrNull()
            }
        }
    }

    fun restartTutorial(screen: Screen) {
        currentScreen = screen
        steps = when (screen) {
            Screen.Habits -> TutorialSteps.habits
            Screen.Emotions -> TutorialSteps.emotions
            Screen.Settings -> TutorialSteps.settings
            else -> emptyList()
        }
        stepIndex = 0
        _currentStep.value = steps.firstOrNull()
    }

    fun proceedToNextStep() {
        if (stepIndex + 1 >= steps.size) {
            Log.d("TutorialVM", "Finished tutorial via proceedToNextStep")
            finishTutorial()
            return
        }
        stepIndex++
        Log.d("TutorialVM", "Proceeding to step $stepIndex / ${steps.size}")
        _currentStep.value = steps[stepIndex]
    }

    fun finishTutorial() {
        Log.d("TutorialVM", "Finishing tutorial for $currentScreen / $currentHomeSection")
        _currentStep.value = null
        viewModelScope.launch {
            val uid = uidProvider()
            Log.d("TutorialVM", "Calling saveTutorialStatus for $currentScreen")
            val original = observeStatus(uid).first()
            val updated = original.markTutorialSeen(currentScreen, currentHomeSection)
            logUpdatedFlags(updated)
            onboardingRepo.saveStatus(uid, updated)
        }
    }

    private fun logUpdatedFlags(status: com.app.domain.entities.OnboardingStatus) {
        Log.d("TutorialVM", "Marking:")
        Log.d("TutorialVM", "  HomeScreenMain = ${status.hasSeenTutorial_HomeScreenMain}")
        Log.d("TutorialVM", "  HomeScreenStats = ${status.hasSeenTutorial_HomeScreenStats}")
        Log.d("TutorialVM", "  HabitsScreen = ${status.hasSeenTutorial_HabitsScreen}")
        Log.d("TutorialVM", "  EmotionsScreen = ${status.hasSeenTutorial_EmotionsScreen}")
        Log.d("TutorialVM", "  SettingsScreen = ${status.hasSeenTutorial_SettingsScreen}")
    }
}

/**
 * Extensión para actualizar solo la flag relevante, sin pisar las demás.
 */
private fun com.app.domain.entities.OnboardingStatus.markTutorialSeen(
    screen: Screen?,
    homeSection: TutorialViewModel.HomeTutorialSection?
): com.app.domain.entities.OnboardingStatus {
    return when {
        screen == Screen.Habits -> copy(hasSeenTutorial_HabitsScreen = true)
        screen == Screen.Emotions -> copy(hasSeenTutorial_EmotionsScreen = true)
        screen == Screen.Settings -> copy(hasSeenTutorial_SettingsScreen = true)
        screen == Screen.Home && homeSection == TutorialViewModel.HomeTutorialSection.Main ->
            copy(hasSeenTutorial_HomeScreenMain = true)
        screen == Screen.Home && homeSection == TutorialViewModel.HomeTutorialSection.Stats ->
            copy(hasSeenTutorial_HomeScreenStats = true)
        else -> this
    }
}
