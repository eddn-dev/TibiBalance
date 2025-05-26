/* ui/screens/settings/SettingsViewModel.kt */
package com.app.tibibalance.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.User
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.auth.SignOutUseCase
import com.app.domain.usecase.user.ObserveUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepo      : AuthRepository,
    observeUser   : ObserveUser,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    /* ---------- UI ---------- */
    data class UiState(
        val loading   : Boolean = true,
        val user      : User?   = null,
        val error     : String? = null,
        val signingOut: Boolean = false
    )

    /** Evento one–shot de logout */
    private val _loggedOut = MutableSharedFlow<Unit>(replay = 1)
    val loggedOut: SharedFlow<Unit> = _loggedOut

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        /* 1️⃣  Escuchar authState y conmutar al flujo de usuario */
        authRepo.authState()                             // Flow<String?>
            .flatMapLatest { uid ->
                if (uid == null) {
                    // Emitimos logout y devolvemos un flow “vacío” para el user
                    _loggedOut.emit(Unit)
                    flowOf<User?>(null)
                } else {
                    observeUser(uid)                     // Flow<User>
                }
            }
            .onEach { user ->
                _ui.value = if (user == null) {
                    UiState(error = "Sin sesión")        // ← o loading = false
                } else {
                    UiState(loading = false, user = user)
                }
            }
            .catch { e ->
                _ui.value = UiState(loading = false, error = e.message)
            }
            .launchIn(viewModelScope)
    }

    /* 2️⃣  Sign-out */
    fun signOut() = viewModelScope.launch {
        _ui.update { it.copy(signingOut = true) }
        signOutUseCase()
        _ui.update { it.copy(signingOut = false) }
        // No es necesario emitir aquí: authState() emitirá null y flatMapLatest ya lo maneja
    }
}
