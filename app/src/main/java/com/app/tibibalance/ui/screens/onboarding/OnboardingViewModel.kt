/**
 * @file    OnboardingViewModel.kt
 * @ingroup ui_screens_onboarding
 * @brief   Maneja animaciones + estado de onboarding, SIN inyectar UID.
 */
package com.app.tibibalance.ui.screens.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.app.domain.entities.OnboardingStatus
import com.app.domain.repository.AuthRepository          // ← nuevo
import com.app.domain.usecase.onboarding.ObserveOnboardingStatus
import com.app.domain.usecase.onboarding.SaveOnboardingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import com.app.tibibalance.R
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application      : Application,
    private val auth : AuthRepository,           // ← obtiene el UID desde aquí
    private val observeStatus  : ObserveOnboardingStatus,
    private val saveStatus     : SaveOnboardingStatus
) : AndroidViewModel(application) {

    /* ─── 1) Animaciones Lottie (sin cambios) ────────────────────────── */
    private val pagesRaw = listOf(R.raw.anim_health, R.raw.anim_habit, R.raw.anim_stats)

    private val _comps = MutableStateFlow(List<LottieComposition?>(pagesRaw.size) { null })
    val compositions: StateFlow<List<LottieComposition?>> = _comps

    init { preloadComps() }

    private fun preloadComps() {
        pagesRaw.forEachIndexed { idx, raw ->
            LottieCompositionFactory.fromRawRes(getApplication(), raw)
                .addListener { comp ->
                    _comps.update { it.toMutableList().also { l -> l[idx] = comp } }
                }
        }
    }

    /* ─── 2) UID y status de onboarding ─────────────────────────────── */

    /** UID reactivo; se emite sólo si hay usuario logueado. */
    private val uidFlow: Flow<String> = auth.authState().filterNotNull()

    /** Flow del estado de onboarding del usuario actual. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val status: StateFlow<OnboardingStatus> =
        uidFlow.flatMapLatest { uid -> observeStatus(uid) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingStatus())

    /* ─── 3) Actions ────────────────────────────────────────────────── */

    fun completeTutorial() = viewModelScope.launch {
        val now = Clock.System.now()
        val uid = uidFlow.first()                           // esperamos UID
        saveStatus(uid, status.value.copy(
            tutorialCompleted = true,
            completedAt       = now
        ))
    }
}
