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

// ui/screens/settings/SettingsViewModel.kt
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepo     : AuthRepository,
    private val observeUser  : ObserveUser,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    data class UiState(
        val loading   : Boolean = true,
        val user      : User?   = null,
        val error     : String? = null,
        val signingOut: Boolean = false
    )

    private val _ui = MutableStateFlow(UiState())
    val  ui : StateFlow<UiState> = _ui

    /* evento one–shot para que la pantalla navegue a Launch */
    // SettingsViewModel.kt  ── solo la línea que declara el SharedFlow
    private val _loggedOut = MutableSharedFlow<Unit>(
        replay = 1,                      // ←  guarda el último valor
        extraBufferCapacity = 0,
        onBufferOverflow   = BufferOverflow.DROP_OLDEST
    )
    val loggedOut: SharedFlow<Unit> = _loggedOut


    init {
        viewModelScope.launch {
            authRepo.authState().collect { uid ->
                if (uid == null) {
                    _loggedOut.emit(Unit)          // 👈  ya no hay usuario
                } else {
                    observeUser(uid)
                        .onEach { _ui.value = UiState(loading = false, user = it) }
                        .catch { e -> _ui.value = UiState(loading = false, error = e.message) }
                        .collect()
                }
            }
        }
    }

    fun signOut() = viewModelScope.launch {
        _ui.update { it.copy(signingOut = true) }
        signOutUseCase()                           // ← hace authRepo.signOut()
        _ui.update { it.copy(signingOut = false) } // el flujo authState() disparará loggedOut
    }
}
