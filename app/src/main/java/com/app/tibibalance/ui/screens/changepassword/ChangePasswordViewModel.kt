/* ui/screens/changepassword/ChangePasswordViewModel.kt */
package com.app.tibibalance.ui.screens.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.auth.ChangePasswordUseCase
import com.app.domain.util.PasswordValidator
import com.google.firebase.auth.EmailAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePwd : ChangePasswordUseCase,
    private val authRepo  : AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _ui

    /* Sólo se muestra en cuentas email/password */
    val canChangePassword: Boolean = authRepo.currentProvider()
        ?.let { it == EmailAuthProvider.PROVIDER_ID } ?: false

    /* ---- input handlers ---- */
    fun onCurrentChange(text: String) = _ui.update { it.copy(current = text, error = null) }

    fun onNewChange(text: String) = _ui.update { state ->
        val strength = PasswordValidator.validateStrength(text)
        val mismatch = if (state.confirm.isNotEmpty() && text != state.confirm)
            "Las contraseñas no coinciden" else null
        state.copy(
            newPass       = text,
            strengthError = strength,
            mismatchError = mismatch,
            error         = null
        )
    }

    fun onConfirmChange(text: String) = _ui.update { state ->
        val mismatch = if (state.newPass.isNotEmpty() && text != state.newPass)
            "Las contraseñas no coinciden" else null
        state.copy(
            confirm       = text,
            mismatchError = mismatch,
            error         = null
        )
    }

    /* ---- acciones ---- */
    fun consumeError()  = _ui.update { it.copy(error = null) }
    fun clearSuccess()  = _ui.update { it.copy(success = false) }

    fun changePassword() {
        val state = _ui.value

        if (state.strengthError != null || state.mismatchError != null) return

        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }

            when (val res = changePwd(state.current, state.newPass)) {
                is AuthResult.Success -> _ui.value = ChangePasswordUiState(success = true)
                is AuthResult.Error   -> _ui.update {
                    it.copy(isLoading = false, error = res.error.toString())
                }
            }
        }
    }
}
