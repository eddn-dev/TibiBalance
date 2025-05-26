/* ui/screens/profile/EditProfileViewModel.kt */
package com.app.tibibalance.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.remote.PhotoUploader
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.user.ObserveUser
import com.app.domain.usecase.user.UpdateUserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepo   : AuthRepository,
    private val observeUser: ObserveUser,
    private val updateUser : UpdateUserProfile,
    private val uploader   : PhotoUploader            // ⬅︎ helper (ver sección 5)
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState(loading = true))
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        // Suscríbete al perfil
        viewModelScope.launch {
            authRepo.authState().collectLatest { uid ->
                if (uid == null) return@collectLatest
                observeUser(uid)
                    .onEach { u -> _state.update { it.copy(user = u, loading = false) } }
                    .collect()
            }
        }
    }

    /* ───────── acciones UI ───────── */

    fun pickPhoto(uri: Uri) {
        _state.update { it.copy(photoUri = uri.toString()) }
    }

    fun save(name: String, dob: LocalDate?, newPhoto: Uri?) = viewModelScope.launch {
        val uid = authRepo.authState().first() ?: return@launch
        _state.update { it.copy(loading = true, error = null, success = false) }

        // 1. si hay foto nueva ⇒ súbela y obtén URL
        val url = newPhoto?.let { uploader.upload(uid, it) }

        // 2. actualiza Firestore + Room
        val res = updateUser(uid, name, dob, url).onFailure { ex ->
            _state.update { it.copy(loading = false, error = ex.message) }
        }.isSuccess

        if (res) {
            _state.update { it.copy(loading = false, success = true, photoUri = null) }
        }
    }

    fun consumeError()  { _state.update { it.copy(error   = null) } }
    fun consumeSuccess(){ _state.update { it.copy(success = false) } }

    val canChangePassword: Boolean
        get() = authRepo.currentProvider() != "google.com"   // helper en AuthRepo
}
