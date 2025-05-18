package com.app.tibibalance.ui.screens.habits.addHabitWizard

/**
 * @file    AddHabitUiState.kt
 * @ingroup ui_wizard
 * @brief   Estados del asistente de creación/edición de hábitos.
 */
import com.app.domain.config.NotifConfig
import com.app.domain.entities.HabitTemplate
import com.app.domain.error.BasicError            // ← enum de dominio
import com.app.domain.entities.HabitForm

/**
 * Todos los momentos posibles en el flujo del wizard.
 *
 * La UI (AddHabitModal) observa un único `StateFlow<AddHabitUiState>`
 * emitido por `AddHabitViewModel`, y decide qué pantalla/renderizar
 * según la sub–clase que reciba.
 */
sealed interface AddHabitUiState {

    /* ───────── Paso 0 · Sugerencias ───────── */
    data class Suggestions(
        /** Borrador que el usuario podría haber comenzado antes. */
        val draft: HabitForm = HabitForm()
    ) : AddHabitUiState

    /* ───────── Paso 1 · Información básica ───────── */
    data class BasicInfo(
        val form        : HabitForm,
        val errors      : List<BasicError> = emptyList(),
        /** Para mostrar el error de “nombre obligatorio” sólo si el
         *  usuario ya tocó el campo al menos una vez. */
        val nameTouched : Boolean = false
    ) : AddHabitUiState

    /* ───────── Paso 2 · Parámetros de seguimiento ───────── */
    data class Tracking(
        val form       : HabitForm,
        /** En este paso usamos textos literales para varios mensajes:
         *  duración, periodo, días, modo reto, etc. */
        val errors     : List<String> = emptyList(),
        /** Si el usuario ya configuró notificaciones antes de llegar
         *  explícitamente al paso 3, guardamos ese borrador aquí. */
        val draftNotif : NotifConfig? = null
    ) : AddHabitUiState

    /* ───────── Paso 3 · Notificaciones ───────── */
    data class Notification(
        val form: HabitForm,
        val cfg : NotifConfig
    ) : AddHabitUiState

    /* ───────── Diálogo intermedio ───────── */
    data class ConfirmDiscard(
        /** Plantilla que el usuario intenta aplicar. */
        val pendingTemplate: HabitTemplate,
        /** Estado previo al diálogo para volver atrás si cancela. */
        val previous       : AddHabitUiState
    ) : AddHabitUiState

    /* ───────── Estados de proceso / resultado ───────── */
    object  Saving               : AddHabitUiState
    data class Saved(val title: String, val message: String) : AddHabitUiState
    data class Error(val msg: String)                        : AddHabitUiState
}
