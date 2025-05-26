/* ui/screens/settings/SettingsViewModel.kt */
package com.app.tibibalance.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.auth.SignOutUseCase
import com.app.domain.usecase.user.ObserveUser
import com.app.domain.usecase.user.UpdateUserSettings        // ⬅️ nuevo
import com.app.tibibalance.ui.theme.ThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepo           : AuthRepository,
    observeUser        : ObserveUser,
    private val update : UpdateUserSettings,
    private val theme  : ThemeController,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    /* ---------- UI ---------- */
    data class UiState(
        val loading   : Boolean = true,
        val user      : User?   = null,
        val error     : String? = null,
        val signingOut: Boolean = false
    )

    /* logout one-shot */
    private val _loggedOut = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val  loggedOut: SharedFlow<Unit> = _loggedOut

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        /* observa cambios de sesión y del documento de usuario */
        authRepo.authState()
            .flatMapLatest { uid ->
                if (uid == null) {
                    _loggedOut.tryEmit(Unit)
                    flowOf<User?>(null)
                } else observeUser(uid)
            }
            .onEach { usr ->
                _ui.value = if (usr == null)
                    UiState(error = "Sin sesión")
                else
                    UiState(loading = false, user = usr)
            }
            .catch { e -> _ui.value = UiState(loading = false, error = e.message) }
            .launchIn(viewModelScope)
    }

    /* --------- acciones --------- */

    fun changeTheme(mode: ThemeMode) {
        theme.setMode(mode)
        persist { old -> old.copy(
            settings = old.settings.copy(theme = mode)
        )}
    }

    fun toggleGlobalNotif(enabled: Boolean) = persist { old ->
        old.copy(settings = old.settings.copy(notifGlobal = enabled))
    }

    fun toggleTTS(enabled: Boolean) = persist { old ->
        old.copy(settings = old.settings.copy(accessibilityTTS = enabled))
    }

    /** Aplica la transformación y la sube a Firestore/Room */
    private fun persist(transform: (User) -> User) = viewModelScope.launch {
        val current = _ui.value.user ?: return@launch
        val updated = transform(current)

        /* Optimistic UI */
        _ui.value = _ui.value.copy(user = updated)

        /* Persiste settings únicamente */
        update(current.uid, updated.settings)
            .onFailure { ex ->
                // Revertir local si falla
                _ui.value = _ui.value.copy(user = current, error = ex.message)
            }
    }

    fun signOut() = viewModelScope.launch {
        _ui.update { it.copy(signingOut = true) }
        signOutUseCase()
        _ui.update { it.copy(signingOut = false) }
        /* authState() emitirá null → _loggedOut */
    }
}
