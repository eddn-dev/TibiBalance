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
import com.app.domain.usecase.user.ObserveUser
import com.app.domain.usecase.user.UpdateUserSettings        // ⬅️ nuevo
import com.app.tibibalance.ui.theme.ThemeController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    authRepo           : AuthRepository,
    observeUser        : ObserveUser,
    private val update : UpdateUserSettings,
    private val theme  : ThemeController,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    /* ---------- UI ---------- */
    data class UiState(
        val loading   : Boolean = true,
        val user      : User?   = null,
        val error     : String? = null,
        val signingOut: Boolean = false,
        val navigatingToGoodbye: Boolean = false
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

    fun reauthenticateAndDelete(password: String? = null, googleIdToken: String? = null) = viewModelScope.launch {
        val TAG = "DeleteAccount"
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.e(TAG, "LAYTON Usuario actual es null.")
            _ui.update { it.copy(error = "No hay sesión activa.") }
            return@launch
        }

        val providerId = user.providerData.getOrNull(1)?.providerId
        Log.d(TAG, "LAYTON Proveedor de autenticación: $providerId")

        try {
            when (providerId) {
                "password" -> {
                    val email = user.email
                    if (email == null || password.isNullOrBlank()) {
                        Log.e(TAG, "LAYTON Email o contraseña faltante. email=$email, password vacío=${password.isNullOrBlank()}")
                        _ui.update { it.copy(error = "Falta email o contraseña.") }
                        return@launch
                    }
                    Log.d(TAG, "LAYTON eautenticando con email/password")
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.reauthenticate(credential).await()
                }

                "google.com" -> {
                    if (googleIdToken.isNullOrBlank()) {
                        Log.e(TAG, "LAYTON idToken de Google es null o vacío.")
                        _ui.update { it.copy(error = "Token de Google inválido.") }
                        return@launch
                    }
                    Log.d(TAG, "LAYTON Reautenticando con Google token")
                    val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    user.reauthenticate(credential).await()
                }

                else -> {
                    Log.e(TAG, "LAYTON Proveedor no soportado: $providerId")
                    _ui.update { it.copy(error = "Proveedor no soportado para eliminar cuenta.") }
                    return@launch
                }
            }

            Log.d(TAG, "LAYTON Reautenticación exitosa. Eliminando cuenta...")
            user.delete().await()
            Log.d(TAG, "LAYTON Cuenta eliminada con éxito.")
            _ui.update { it.copy(navigatingToGoodbye = true) }

        } catch (e: Exception) {
            Log.e(TAG, "LAYTON Error durante reautenticación o eliminación", e)
            _ui.update { it.copy(error = "No se pudo eliminar: ${e.message ?: "Error desconocido"}") }
        }
    }


    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
