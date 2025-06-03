/* ui/theme/AppThemeViewModel.kt */
package com.app.tibibalance.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.enums.ThemeMode
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.user.ObserveUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val theme   : ThemeController,   // mismo singleton
    authRepo            : AuthRepository,
    observeUser         : ObserveUser
) : ViewModel() {

    /** la UI solo necesita leer este flujo */
    val mode: StateFlow<ThemeMode> = theme.mode

    init {
        /* ①  Escucha sesión, ②  lee ajustes del usuario            */
        /* ③  actualiza themeController -> la UI se recompone.       */
        authRepo.authState()
            .flatMapLatest { uid ->
                if (uid == null) flowOf<ThemeMode>(ThemeMode.SYSTEM)
                else observeUser(uid).map { it.settings.theme }
            }
            .onEach(theme::setMode)
            .launchIn(viewModelScope)
    }
}
