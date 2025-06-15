package com.app.tibibalance.ui.screens.settings.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.data.alert.EmotionAlertManager
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.Habit
import com.app.domain.entities.User
import com.app.domain.ids.HabitId
import com.app.domain.usecase.habit.GetHabitById
import com.app.domain.usecase.habit.GetHabitsFlow
import com.app.domain.usecase.habit.UpdateHabit
import com.app.domain.usecase.user.ObserveUser
import com.app.domain.usecase.user.UpdateUserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import javax.inject.Inject

/* ───────────── modelos UI ───────────── */

data class HabitNotifUi(
    val id      : String,
    val name    : String,
    val icon    : String,
    val enabled : Boolean
)

sealed class ConfigureNotifUiState {
    object Loading                         : ConfigureNotifUiState()
    object Empty                           : ConfigureNotifUiState()
    data class Loaded(val data: List<HabitNotifUi>) : ConfigureNotifUiState()
    data class Error(val msg: String)      : ConfigureNotifUiState()
}

/* ───────────── ViewModel ───────────── */

@HiltViewModel
class ConfigureNotificationViewModel @Inject constructor(
    getHabitsFlow      : GetHabitsFlow,
    private val getHabitById   : GetHabitById,
    private val updateHabit    : UpdateHabit,
    observeUser         : ObserveUser,
    private val updateSettings : UpdateUserSettings,
    uidProvider         : AuthUidProvider,
    private val emotionMgr     : EmotionAlertManager
) : ViewModel() {

    /* ---------- listado reactivo ---------- */
    val ui: StateFlow<ConfigureNotifUiState> = getHabitsFlow()
        .map { list ->
            val items = list.map { it.toUi() }
            if (items.isEmpty()) ConfigureNotifUiState.Empty
            else                 ConfigureNotifUiState.Loaded(items)
        }
        .catch { emit(ConfigureNotifUiState.Error(it.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            ConfigureNotifUiState.Loading)

    /* ---------- hábito seleccionado para el modal ---------- */
    private val _selectedHabit = MutableStateFlow<HabitNotifUi?>(null)
    val selectedHabit: StateFlow<HabitNotifUi?> = _selectedHabit.asStateFlow()
    fun selectHabit(habit: HabitNotifUi) = _selectedHabit.update { habit }
    fun clearSelectedHabit()            = _selectedHabit.update { null }

    /* ---------- notificación de emociones ---------- */
    private val _notifEmotion = MutableStateFlow(true)
    val notifEmotion: StateFlow<Boolean> = _notifEmotion.asStateFlow()

    private val _emotionTime = MutableStateFlow<LocalTime?>(null)
    val emotionTime: StateFlow<LocalTime?> = _emotionTime.asStateFlow()

    private val userFlow: Flow<User?> = observeUser(uidProvider())

    init {
        userFlow.onEach { usr ->
            usr?.settings?.let {
                _notifEmotion.value = it.notifEmotion
                _emotionTime.value  = it.notifEmotionTime?.let(LocalTime::parse)
            }
        }.launchIn(viewModelScope)
    }

    /* ---------- habilitar / deshabilitar campana ---------- */
    fun toggleNotification(habitUi: HabitNotifUi) = viewModelScope.launch {
        val habit = getHabitById(HabitId(habitUi.id)).first() ?: return@launch
        val updated = habit.copy(
            notifConfig = habit.notifConfig.copy(enabled = !habit.notifConfig.enabled),
            meta        = habit.meta.copy(
                updatedAt   = Clock.System.now(),
                pendingSync = true
            )
        )
        updateHabit(updated)
        // 🔔  SIN disparar logros aquí
    }

    /* ---------- toggle ON/OFF recordatorio emociones ---------- */
    fun toggleEmotionNotif() = viewModelScope.launch {
        val user = userFlow.first() ?: return@launch
        val newVal = !_notifEmotion.value

        val newSettings = user.settings.copy(notifEmotion = newVal)
        updateSettings(user.uid, newSettings).onSuccess {
            _notifEmotion.value = newVal
            if (newVal && _emotionTime.value != null)
                emotionMgr.schedule(_emotionTime.value!!)
            else
                emotionMgr.cancel()
        }
    }

    fun updateEmotionTime(newTime: LocalTime) = viewModelScope.launch {
        val user = userFlow.first() ?: return@launch
        _emotionTime.value = newTime

        val newSettings = user.settings.copy(notifEmotionTime = newTime.toString())
        updateSettings(user.uid, newSettings).onSuccess {
            if (_notifEmotion.value) emotionMgr.schedule(newTime)
        }
        // 🔔  SIN disparar logros aquí
    }

    /* ---------- mapper dominio ➜ UI ---------- */
    private fun Habit.toUi() = HabitNotifUi(
        id      = id.value,
        name    = name,
        icon    = icon,
        enabled = notifConfig.enabled
    )
}
