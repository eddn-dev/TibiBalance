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
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.EmotionEntry
import com.app.domain.usecase.emotions.ObserveEmotions
import com.app.domain.usecase.emotions.SaveEmotion
import com.app.tibibalance.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import java.time.ZoneId

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
) : ViewModel() {

    /* ---------- UI State ---------- */

    private val _ui = MutableStateFlow<EmotionalUiState>(EmotionalUiState.Loading)
    val ui: StateFlow<EmotionalUiState> = _ui

    private val _dialog = MutableStateFlow<DialogState>(DialogState.None)
    val dialog: StateFlow<DialogState> = _dialog

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
                _ui.value   = EmotionalUiState.Error(e.message ?: "Error")
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
                DialogState.Info("No puedes registrar emociones de días futuros.")
            else ->
                DialogState.Info("Sólo puedes registrar la emoción de hoy.")
        }
    }

    /** Guarda la emoción seleccionada y cierra el diálogo. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmEmotion(date: java.time.LocalDate, emotion: Emotion) = viewModelScope.launch {
        saveEmotion(
            /* date -> kotlinx.datetime.LocalDate */
            LocalDate(date.year, date.monthValue, date.dayOfMonth),
            emojiId = emotion.name
        )
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
                iconRes      = entry?.emojiId?.let(::emojiDrawable),
                isRegistered = entry != null
            )
        }
    }

    /** Convierte el id “FELICIDAD” → R.drawable.ic_happyimage. */
    private fun emojiDrawable(id: String): Int = when (id) {
        Emotion.FELICIDAD.name    -> R.drawable.ic_happyimage
        Emotion.TRANQUILIDAD.name -> R.drawable.ic_calmimage
        Emotion.TRISTEZA.name     -> R.drawable.ic_sadimage
        Emotion.ENOJO.name        -> R.drawable.ic_angryimage
        Emotion.DISGUSTO.name     -> R.drawable.ic_disgustingimage
        Emotion.MIEDO.name        -> R.drawable.ic_fearimage
        else                      -> 0
    }
}
