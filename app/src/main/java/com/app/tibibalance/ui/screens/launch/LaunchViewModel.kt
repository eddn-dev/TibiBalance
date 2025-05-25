/**
 * @file      LaunchViewModel.kt
 * @ingroup   ui_screens_launch
 * @brief     ViewModel que decide el flujo inicial (auth / verify / main).
 *
 * @details
 *  - Observa `AuthRepository.authState` (UID o null) y el flag `isEmailVerified`
 *    de `FirebaseAuth.currentUser`.
 *  - Expone `SessionState` como `StateFlow` para que la UI (LaunchScreen)
 *    redirija reactivamente.
 *  - Cuando hay usuario logueado llama a `refreshAndSync()` para
 *    recargar FirebaseAuth y, si procede, sincronizar la verificación en Firestore.
 */
package com.app.tibibalance.ui.screens.launch

import android.util.Log
import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.repository.AuthRepository        // <- interfaz en :domain
import com.app.domain.usecase.auth.SyncAccount
import com.app.domain.usecase.onboarding.ObserveOnboardingStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val authRepo : AuthRepository,
    private val observeOnb: ObserveOnboardingStatus,
    private val syncAccount: SyncAccount,
    private val auth     : FirebaseAuth
) : ViewModel() {

    private val syncedUid = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessionState: StateFlow<SessionState?> =
        authRepo.authState()
            .flatMapLatest { uid ->
                when (uid) {
                    null -> flowOf(SessionState(loggedIn = false))
                    else -> {
                        refreshOnce()
                        launchSyncIfNeeded(uid)      // disspara SyncAccount

                        observeOnb(uid).map { onb ->
                            SessionState(
                                loggedIn            = true,
                                verified            = auth.currentUser?.isEmailVerified == true,
                                onboardingCompleted = onb.tutorialCompleted
                            )
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    /* ------------------------------------------------------------------ */
    private fun launchSyncIfNeeded(uid: String) {
        if (syncedUid.value == uid) return            // ya intentamos ⇢ evita bucle infinito
        viewModelScope.launch {
            val result = syncAccount()                // 🔄 push-pull
            if (result.isSuccess) {
                syncedUid.value = uid                 // ✅ sólo ahora sellamos el UID
            } else {
                Log.e(result.exceptionOrNull().toString(), "SyncAccount failed")
                // Si quieres reintentos automáticos:
                // delay(5_000); launchSyncIfNeeded(uid)
            }
        }
    }


    private fun refreshOnce() = viewModelScope.launch {
        try { auth.currentUser?.reload()?.await() } catch (_: Exception) {}
    }
}


data class SessionState(
    val loggedIn           : Boolean = false,
    val verified           : Boolean = false,
    val onboardingCompleted: Boolean? = null   // 👈 NEW (null = “aún no sabemos”)
)
