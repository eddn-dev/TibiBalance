/* ui/screens/settings/EditNotifViewModel.kt */
package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.mappers.toForm
import com.app.data.mappers.toHabit
import com.app.domain.achievements.event.AchievementEvent
import com.app.domain.entities.Achievement
import com.app.domain.ids.HabitId
import com.app.domain.model.HabitForm
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.achievement.CheckUnlockAchievement
import com.app.domain.usecase.habit.GetHabitById
import com.app.domain.usecase.habit.UpdateHabit
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
    private val getHabit        : GetHabitById,
    private val updateHabit     : UpdateHabit,
    private val checkAchievement: CheckUnlockAchievement,   // 🆕
    private val auth            : AuthRepository
) : ViewModel() {

    /* ----------- carga explícita del hábito ----------- */
    private val habitId = MutableStateFlow<HabitId?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val habit = habitId.filterNotNull()
        .flatMapLatest { getHabit(it) }

    /* ----------- estado del formulario --------------- */
    private val _form = MutableStateFlow(HabitForm())
    val  form : StateFlow<HabitForm> = _form

    /* ----------- flags UI ---------------------------- */
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    /* ----------- logros desbloqueados ---------------- */
    private val _unlocked = MutableSharedFlow<AchievementUnlocked>(extraBufferCapacity = 1)
    val unlocked: SharedFlow<AchievementUnlocked> = _unlocked

    /* ------------ API pública ------------------------ */

    fun load(id: HabitId) = viewModelScope.launch {
        habitId.value = id
        habit.filterNotNull().first().let { h -> _form.value = h.toForm() }
    }

    fun onFormChanged(f: HabitForm) { _form.value = f }

    /** Guarda únicamente `notifConfig` y emite logros si procede. */
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

        /* ----- motor de logros ----- */
        val notifyConfigured = _form.value.notify && _form.value.notifTimes.isNotEmpty()
        if (notifyConfigured) {
            checkAchievement(AchievementEvent.NotifCustomized)
                .forEach { ach -> _unlocked.emit(ach.toUi()) }
        }

        _saving.value = false
    }

    /* ------------- helpers --------------------------- */

    var wasSaveTriggered = false
        private set

    fun guardarYVerificarLogro(): Boolean {
        wasSaveTriggered = true
        save()                     // espera a terminar
        return unlocked.replayCache.isNotEmpty()
    }

    /* Achievement → UI DTO */
    private fun Achievement.toUi() =
        AchievementUnlocked(id.raw, name, description)
}
