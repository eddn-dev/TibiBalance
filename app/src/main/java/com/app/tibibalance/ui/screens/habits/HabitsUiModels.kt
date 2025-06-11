/**
 * @file    HabitsUiModels.kt
 * @ingroup ui_screens_habits
 * @brief   Modelos y eventos propios de la pantalla Habits.
 *
 * @details
 *  - `HabitsUiState` describe los estados que puede mostrar la UI.
 *  - `HabitsEvent`  son notificaciones one-shot (navegación, snackbars, etc.).
 *  - `HabitUi`      es la representación mínima que la lista necesita.
 */

package com.app.tibibalance.ui.screens.habits

/* ─────────────────────────────── UI-STATE ─────────────────────────────── */

/**
 * Estados posibles de la pantalla de hábitos.
 *
 * @see HabitsViewModel.uiState
 */
sealed interface HabitsUiState {
    /** Cargando desde la BD local. Se muestra un spinner o placeholder. */
    object Loading                       : HabitsUiState

    /** No existen hábitos: se enseña estado vacío con botón “Crear”. */
    object Empty                         : HabitsUiState

    /**
     * Lista cargada con éxito.
     * @param data  Colección de [HabitUi] ordenada por categoría/nombre.
     */
    data class Loaded(val data: List<HabitUi>) : HabitsUiState

    /**
     * Fallo irrecuperable (p.e. base de datos corrupta).
     * @param msg  Texto de error que mostrará la UI.
     */
    data class Error(val msg: String)    : HabitsUiState
}

/* ─────────────────────────────── EVENTS ───────────────────────────────── */

/**
 * Eventos “one-shot” emitidos por el ViewModel.
 *
 * Se reciben en la UI usando `LaunchedEffect` + `collect`.
 */
sealed interface HabitsEvent {
    /** El usuario pulsó el FAB; abre el wizard “Crear hábito”. */
    object AddClicked : HabitsEvent

    /**
     * El usuario pulsó un elemento para ver/editar detalles.
     * @param habitId  Identificador del hábito seleccionado.
     */
    data class ShowDetails(val habitId: String) : HabitsEvent
}

/* ─────────────────────────────── UI-MODEL ─────────────────────────────── */

/**
 * Versión simplificada de [com.app.domain.entities.Habit] para la lista.
 */
data class HabitUi(
    val id       : String,  ///< `HabitId.value`
    val name     : String,
    val icon     : String,  ///< Nombre del material-icon (ej. `"ic_favorite"`)
    val category : String,  ///< Enum en texto (“SALUD”, “PRODUCTIVIDAD”, …)
    val challenge: Boolean,
)
