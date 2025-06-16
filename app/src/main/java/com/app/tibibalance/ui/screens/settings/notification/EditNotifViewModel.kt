@file:OptIn(ExperimentalCoroutinesApi::class)

package com.app.tibibalance.ui.screens.settings.notification

import android.os.Build
import android.util.Log
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
    private val checkAchievement: CheckUnlockAchievement,
    private val auth            : AuthRepository
) : ViewModel() {

    /* ---------- flujo del hábito ---------- */
    private val habitId = MutableStateFlow<HabitId?>(null)
    private val habit   = habitId.filterNotNull().flatMapLatest { getHabit(it) }

    /* ---------- formulario ---------- */
    private val _form = MutableStateFlow(HabitForm())
    val   form : StateFlow<HabitForm> = _form

    /* ---------- estados UI ---------- */
    private val _saving   = MutableStateFlow(false)
    val   saving  : StateFlow<Boolean> = _saving

    private val _savedOk = MutableStateFlow(false)   // 🆕
    val   savedOk: StateFlow<Boolean> = _savedOk
    fun consumeSaved() { _savedOk.value = false }

    /* ---------- cola de logros ---------- */
    private val pending    = mutableListOf<AchievementUnlocked>()
    private val _unlocked  = MutableSharedFlow<AchievementUnlocked>(extraBufferCapacity = 1)
    val   unlocked: SharedFlow<AchievementUnlocked> = _unlocked

    /* ---------- helpers ---------- */
    fun load(id: HabitId) = viewModelScope.launch {
        habitId.value = id
        habit.filterNotNull().first().let { _form.value = it.toForm() }
    }
    fun onFormChanged(f: HabitForm) { _form.value = f }
    fun popNextAchievement(): AchievementUnlocked? = pending.removeFirstOrNull()

    /* ---------- guardar ---------- */
    suspend fun save() {
        val original = habit.first() ?: return
        _saving.value = true

        val updated = _form.value
            .toHabit(original.id, now = Clock.System.now())
            .copy(
                name        = original.name,
                description = original.description,
                category    = original.category,
                icon        = original.icon,
                session     = original.session,
                repeat      = original.repeat,
                period      = original.period,
                challenge   = original.challenge,
                meta        = original.meta.copy(
                    updatedAt   = Clock.System.now(),
                    pendingSync = true
                )
            )

        updateHabit(updated)

        if (updated.notifConfig.enabled && updated.notifConfig.times.isNotEmpty()) {
            checkAchievement(AchievementEvent.NotifCustomized).forEach { ach ->
                val uiAch = ach.toUi()
                pending += uiAch
                _unlocked.emit(uiAch)
            }
        }

        _saving.value = false
        _savedOk.value = true                // 🆕 dispara modal de éxito
    }

    private fun Achievement.toUi() =
        AchievementUnlocked(id.raw, name, description)
}
