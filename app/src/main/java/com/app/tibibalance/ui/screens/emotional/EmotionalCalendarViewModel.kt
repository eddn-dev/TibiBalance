/**
 * @file      EmotionalCalendarViewModel.kt
 * @ingroup   ui_screens_emotional
 * @brief     ViewModel del calendario emocional – única fuente de verdad.
 *
 * @details
 * • Expone dos flujos:
 *     1. [ui]      → estado de la lista/errores (renderizado principal).
 *     2. [dialog]  → estado de diálogos (registrar, info, error).
 * • Contiene toda la lógica de:
 *     – suscripción a registros (`ObserveEmotions`)
 *     – validación de clic (día pasado / futuro / hoy)
 *     – persistencia offline-first (`SaveEmotion`)
 *
 *  UI nunca calcula nada: sólo reacciona a estos StateFlow.
 */
package com.app.tibibalance.ui.screens.emotional

import android.os.Build
import android.os.SharedMemory
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.achievements.event.AchievementEvent
import com.app.domain.entities.Achievement
import com.app.domain.entities.EmotionEntry
import com.app.domain.enums.Emotion
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.achievement.CheckUnlockAchievement
import com.app.domain.usecase.emotions.ObserveEmotions
import com.app.domain.usecase.emotions.SaveEmotion
import com.app.tibibalance.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked

/* ────────────────────────────────────────────────────────────────── */
/* 1 ▸ diálogos                                                        */
/* ────────────────────────────────────────────────────────────────── */

/** Estados posibles de diálogo que la UI debe mostrar. */
sealed interface DialogState {
    object None : DialogState
    data class Register(val date: java.time.LocalDate) : DialogState
    data class Info(val msg: String)                    : DialogState
    data class Error(val msg: String)                   : DialogState
}

/* ────────────────────────────────────────────────────────────────── */
/* 2 ▸ ViewModel                                                      */
/* ────────────────────────────────────────────────────────────────── */

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class EmotionalCalendarViewModel @Inject constructor(
    observeEmotions: ObserveEmotions,
    private val saveEmotion : SaveEmotion,
    private val chechAchievement : CheckUnlockAchievement,
    private val auth: AuthRepository
) : ViewModel() {

    /* ---------- UI State ---------- */

    private val _ui = MutableStateFlow<EmotionalUiState>(EmotionalUiState.Loading)
    val ui: StateFlow<EmotionalUiState> = _ui

    private val _dialog = MutableStateFlow<DialogState>(DialogState.None)
    val dialog: StateFlow<DialogState> = _dialog

    private val _unlocked = MutableSharedFlow<AchievementUnlocked>(extraBufferCapacity = 1)
    val unlocked: SharedFlow<AchievementUnlocked> = _unlocked

    init {
        observeEmotions()
            .map(::mapMonthToUi)
            .onEach { days ->
                _ui.value = if (days.isEmpty())
                    EmotionalUiState.Empty
                else
                    EmotionalUiState.Loaded(days)
            }
            .catch { e ->
                _ui.value = EmotionalUiState.Error(e.message ?: "Error")
                _dialog.value = DialogState.Error(e.message ?: "Error")
            }
            .launchIn(viewModelScope)
    }

    /* ---------- Intents desde la UI ---------- */

    /** Usuario hace tap en un día del calendario. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onCalendarDayClicked(dayUi: EmotionDayUi) {
        val today = java.time.LocalDate.now()
        _dialog.value = when {
            dayUi.day == today.dayOfMonth ->
                DialogState.Register(today)
            dayUi.day  > today.dayOfMonth ->
                DialogState.Info("\uD83D\uDE80Alto ahí viajero del tiempo \uD83D\uDD2E\n" +
                        "Solo puedes registrar \nla emoción de hoy.\uD83D\uDCC5")
            else ->
                DialogState.Info("No te preocupes por el pasado\uD83D\uDDDD\uFE0F\n ¡Registra tu emoción de hoy!✨")
        }
    }

    /** Guarda la emoción seleccionada y cierra el diálogo. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmEmotion(date: java.time.LocalDate, emotion: Emotion) = viewModelScope.launch {
        saveEmotion(
            /* date -> kotlinx.datetime.LocalDate */
            LocalDate(date.year, date.monthValue, date.dayOfMonth),
            emotion
        )

        chechAchievement(
            AchievementEvent.EmotionLogged(
                date = LocalDate(date.year, date.monthValue, date.dayOfMonth),
                mood = emotion
            )
        ).forEach { ach -> _unlocked.emit(ach.toUi()) }
        _dialog.value = DialogState.None       // cierra modal
    }

    /** Descarta el diálogo actual. */
    fun dismissDialog() { _dialog.value = DialogState.None }

    /* ---------- Helpers ---------- */

    /** Transforma la lista de registros a los 1..N días del mes actual. */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapMonthToUi(entries: List<EmotionEntry>): List<EmotionDayUi> {
        val today      = java.time.LocalDate.now()
        val daysInMonth= today.lengthOfMonth()
        val indexed    = entries.associateBy { it.date.dayOfMonth }

        return (1..daysInMonth).map { d ->
            val entry = indexed[d]
            EmotionDayUi(
                day          = d,
                iconRes      = entry?.mood?.let(::emojiDrawable),
                isRegistered = entry != null
            )
        }
    }

    /** Convierte el id “FELICIDAD” → R.drawable.ic_happyimage. */
    private fun emojiDrawable(e: Emotion): Int = when (e) {
        Emotion.FELICIDAD    -> R.drawable.ic_happyimage
        Emotion.TRANQUILIDAD -> R.drawable.ic_calmimage
        Emotion.TRISTEZA     -> R.drawable.ic_sadimage
        Emotion.ENOJO        -> R.drawable.ic_angryimage
        Emotion.DISGUSTO     -> R.drawable.ic_disgustingimage
        Emotion.MIEDO        -> R.drawable.ic_fearimage
        else                      -> 0
    }

    /* helper interno */
    private fun Achievement.toUi() =
        AchievementUnlocked(id.raw, name, description)

}
