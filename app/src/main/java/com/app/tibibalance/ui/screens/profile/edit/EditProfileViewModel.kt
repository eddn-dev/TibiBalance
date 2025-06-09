/* ui/screens/profile/EditProfileViewModel.kt */
package com.app.tibibalance.ui.screens.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.remote.PhotoUploader
import com.app.domain.achievements.event.AchievementEvent
import com.app.domain.entities.Achievement
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.achievement.CheckUnlockAchievement
import com.app.domain.usecase.user.ObserveUser
import com.app.domain.usecase.user.UpdateUserProfile
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepo        : AuthRepository,
    private val observeUser     : ObserveUser,
    private val updateUser      : UpdateUserProfile,
    private val uploader        : PhotoUploader,
    private val checkAchievement: CheckUnlockAchievement          // 🆕
) : ViewModel() {

    /* ──────────────────────── UI State ─────────────────────── */

    private val _state = MutableStateFlow(EditProfileUiState(loading = true))
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    /* Logros desbloqueados */
    private val _unlocked = MutableSharedFlow<AchievementUnlocked>(extraBufferCapacity = 1)
    val unlocked: SharedFlow<AchievementUnlocked> = _unlocked

    init {
        /* Suscribe el documento de usuario */
        viewModelScope.launch {
            authRepo.authState().collectLatest { uid ->
                if (uid == null) return@collectLatest
                observeUser(uid)
                    .onEach { u -> _state.update { it.copy(user = u, loading = false) } }
                    .collect()
            }
        }
    }

    /* ───────────────── Acciones de UI ──────────────────────── */

    fun pickPhoto(uri: Uri) {
        _state.update { it.copy(photoUri = uri.toString()) }
    }

    fun save(
        name    : String,
        dob     : LocalDate?,
        newPhoto: Uri?
    ) = viewModelScope.launch {
        val uid = authRepo.authState().first() ?: return@launch

        _state.update { it.copy(loading = true, error = null, success = false) }

        /* 1 ▸ sube foto si cambió */
        val url = newPhoto?.let { uploader.upload(uid, it) }

        /* 2 ▸ actualiza perfil local + remoto */
        val ok = updateUser(uid, name, dob, url)
            .onFailure { ex -> _state.update { it.copy(loading = false, error = ex.message) } }
            .isSuccess

        if (!ok) return@launch

        /* 3 ▸ disparar motor de logros */
        checkAchievement(
            AchievementEvent.ProfileUpdated(changedPhoto = url != null)
        ).forEach { ach -> _unlocked.emit(ach.toUi()) }

        /* 4 ▸ éxito UI */
        _state.update { it.copy(loading = false, success = true, photoUri = null) }
    }

    /* ───────────────── Helpers ─────────────────────────────── */

    fun consumeError()   = _state.update { it.copy(error   = null) }
    fun consumeSuccess() = _state.update { it.copy(success = false) }

    val canChangePassword: Boolean
        get() = authRepo.currentProvider() != "google.com"

    /* Achievement → modal DTO */
    private fun Achievement.toUi() =
        AchievementUnlocked(id.raw, name, description)
}
