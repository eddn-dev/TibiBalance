/* ui/screens/settings/EditNotifViewModel.kt */
package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.ids.HabitId
import com.app.domain.model.HabitForm
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.habit.GetHabitById
import com.app.domain.usecase.habit.UpdateHabit
import com.app.domain.usecase.user.UnlockAchievementUseCase
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class EditNotifViewModel @Inject constructor(
    private val getHabit   : GetHabitById,
    private val updateHabit: UpdateHabit,
    private val unlockAchievement: UnlockAchievementUseCase,
    private val auth: AuthRepository
) : ViewModel() {

    /* ------ carga explícita ------ */
    private val habitId = MutableStateFlow<HabitId?>(null)
    private val _logroDesbloqueado = MutableStateFlow<AchievementUnlocked?>(null)
    val logroDesbloqueado: StateFlow<AchievementUnlocked?> = _logroDesbloqueado

    @OptIn(ExperimentalCoroutinesApi::class)
    private val habit = habitId.filterNotNull()
        .flatMapLatest { getHabit(it) }

    /* ------ estado del formulario ------ */
    private val _form = MutableStateFlow(HabitForm())
    val  form : StateFlow<HabitForm> = _form

    /* ------ flags UI ------ */
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    fun load(id: HabitId) = viewModelScope.launch {
        habitId.value = id
        habit.filterNotNull().first().let { h -> _form.value = h.toForm() }
    }

    /* se llama desde NotificationStep */
    fun onFormChanged(f: HabitForm) { _form.value = f }

    /** Guarda únicamente `notifConfig` y `meta` */
    fun save() = viewModelScope.launch {
        val h = habit.first() ?: return@launch
        _saving.value = true

        val updated = _form.value.toHabit(h.id, now = Clock.System.now())
            .copy(
                name        = h.name,
                description = h.description,
                category    = h.category,
                icon        = h.icon,
                session     = h.session,
                repeat      = h.repeat,
                period      = h.period,
                challenge   = h.challenge,
                meta        = h.meta.copy(
                    updatedAt   = Clock.System.now(),
                    pendingSync = true
                )
            )

        updateHabit(updated)

        // Lógica de desbloqueo
        val uid = auth.authState().firstOrNull()
        if (uid != null && _form.value.notify && _form.value.notifTimes.isNotEmpty()) {
            val desbloqueado = unlockAchievement(uid, "noti_personalizada")
            if (desbloqueado) {
                _logroDesbloqueado.value = AchievementUnlocked(
                    id = "noti_personalizada",
                    name = "¡Ya es hora!",
                    description = "Descubriste la personalización de notificaciones desde configuración."
                )
            }
        }
        _saving.value = false
    }

    fun ocultarLogro() {
        _logroDesbloqueado.value = null
    }

    var wasSaveTriggered = false
        private set

    suspend fun guardarYVerificarLogro(): Boolean {
        wasSaveTriggered = true
        save()
        return logroDesbloqueado.value != null
    }
}
