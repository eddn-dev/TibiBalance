/* ui/screens/settings/SettingsViewModel.kt */
package com.app.tibibalance.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.auth.DeleteAccountUseCase
import com.app.domain.usecase.auth.SignOutUseCase
import com.app.domain.usecase.auth.SyncAccount
import com.app.domain.usecase.user.ObserveUser
import com.app.domain.usecase.user.UpdateUserSettings        // ⬅️ nuevo
import com.app.tibibalance.ui.theme.ThemeController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepo           : AuthRepository,
    observeUser        : ObserveUser,
    private val update : UpdateUserSettings,
    private val theme  : ThemeController,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val syncAccount: SyncAccount
) : ViewModel() {

    /* ---------- UI ---------- */
    data class UiState(
        val loading   : Boolean = true,
        val user      : User?   = null,
        val error     : String? = null,
        val signingOut: Boolean = false,
        val syncing     : Boolean = false,
        val syncDone    : Boolean = false,
        val navigatingToGoodbye: Boolean = false
    )

    /* logout one-shot */
    private val _loggedOut = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val  loggedOut: SharedFlow<Unit> = _loggedOut

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        authRepo.authState()
            .flatMapLatest { uid ->
                if (uid == null) {
                    _loggedOut.tryEmit(Unit)
                    flowOf<User?>(null)
                } else observeUser(uid)
            }
            .onEach { usr ->
                _ui.update { prev ->                 //  🟢  conserva flags previos
                    when (usr) {
                        null -> prev.copy(
                            loading = false,
                            user    = null,
                            error   = "Sin sesión"
                        )
                        else  -> prev.copy(
                            loading = false,
                            user    = usr,
                            error   = null
                        )
                    }
                }
            }
            .catch { e -> _ui.update { it.copy(loading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }


    /* --------- acciones --------- */

    fun changeTheme(mode: ThemeMode) {
        theme.setMode(mode)
        persist { old -> old.copy(
            settings = old.settings.copy(theme = mode)
        )}
    }

    fun syncNow() = viewModelScope.launch {
        _ui.update { it.copy(syncing = true, syncDone = false) }   // ← limpia bandera previa

        val result = syncAccount()
        _ui.update { it.copy(syncing = false) }

        result.onSuccess {
            _ui.update { it.copy(syncDone = true) }                 // 🆕  muestra modal
        }.onFailure { ex ->
            _ui.update { it.copy(error = ex.message ?: "Error al sincronizar") }
        }
    }

    /*  cierra el modal  */
    fun dismissSyncDone() {
        _ui.update { it.copy(syncDone = false) }
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

    fun deleteAccount() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        val result = deleteAccountUseCase()
        _ui.update { it.copy(loading = false) }

        if (result.isSuccess) {
            _loggedOut.tryEmit(Unit)  // navega a pantalla de inicio
        } else {
            _ui.update { it.copy(error = result.exceptionOrNull()?.message) }
        }
    }

    fun reauthenticateAndDelete(password: String) = viewModelScope.launch {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: return@launch

        val credential = EmailAuthProvider.getCredential(email, password)

        try {
            user.reauthenticate(credential).await()
            user.delete().await()
            _ui.update { it.copy(navigatingToGoodbye = true) }
        } catch (e: Exception) {
            _ui.update { it.copy(error = e.message ?: "Error al eliminar cuenta") }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
