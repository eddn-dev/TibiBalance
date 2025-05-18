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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.repository.AuthRepository        // <- interfaz en :domain
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Estado mínimo que la UI necesita para tomar decisiones. */
data class SessionState(
    val loggedIn: Boolean = false,
    val verified: Boolean = false
)

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    /** Flow de estado de sesión observado por LaunchScreen. */
    val sessionState: StateFlow<SessionState> =
        repo.authState()                   // Flow<String?>  (UID o null)
            .map { uid ->                // Se dispara ante cualquier cambio
                if (uid != null) refreshOnce() // recarga/verifica si hay user
                SessionState(
                    loggedIn = uid != null,
                    verified = auth.currentUser?.isEmailVerified == true
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SessionState()
            )

    /** -- Helpers -------------------------------------------------------- */

    private fun refreshOnce() = viewModelScope.launch {
        try {
            auth.currentUser?.reload()?.await()
            if (auth.currentUser?.isEmailVerified == true) {
                repo.syncVerification()   // sube flag a Firestore si cambia
            }
        } catch (_: Exception) { /* silencio: no rompemos UX offline */ }
    }
}
