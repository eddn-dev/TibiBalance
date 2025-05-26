/* ui/screens/profile/ViewProfileViewModel.kt */
package com.app.tibibalance.ui.screens.profile.show

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.User
import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.user.ObserveUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewProfileViewModel @Inject constructor(
    private val authRepo   : AuthRepository,
    private val observeUser: ObserveUser
) : ViewModel() {

    /* ----------- UI-state ----------- */
    data class UiState(
        val loading : Boolean       = true,
        val user    : User?         = null,
        val error   : String?       = null,
        val signingOut: Boolean     = false
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        /* Observa el UID de la sesión y luego el documento de usuario */
        viewModelScope.launch {
            authRepo.authState().collect { uid ->
                if (uid == null) {                        // sesión cerrada
                    _ui.value = UiState(error = "Sin sesión")
                } else {
                    observeUser(uid).onEach { user ->
                        _ui.value = UiState(
                            loading = false,
                            user    = user
                        )
                    }.catch { e ->
                        _ui.value = UiState(
                            loading = false,
                            error   = e.message ?: "Error"
                        )
                    }.collect()
                }
            }
        }
    }

    /* ----------- acciones ----------- */
    fun signOut() = viewModelScope.launch {
        _ui.update { it.copy(signingOut = true) }
        authRepo.signOut()
        _ui.update { it.copy(signingOut = false) }
    }
}
