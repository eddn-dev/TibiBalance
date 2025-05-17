package com.app.tibibalance.ui.components.utils

import androidx.annotation.DrawableRes

/**
 * @brief Data class que representa el estado y contenido de un día en el [CalendarGrid].
 * @details Contiene la información necesaria para renderizar una celda individual del calendario.
 *
 * @property day El número del día del mes ([Int]). Es `null` si la celda corresponde a un día
 * fuera del mes actual (relleno al principio o final de la cuadrícula).
 * @property emotionRes El ID del recurso drawable ([DrawableRes]) para el icono de la emoción
 * asociada a este día. Es `null` si no hay emoción registrada o si `day` es `null`.
 * @property isSelected Indica si esta celda de día está actualmente seleccionada (`true`)
 * o no (`false`). Afecta al estilo visual de la celda. Por defecto `false`.
 * @property onClick La función lambda que se ejecuta cuando el usuario pulsa sobre esta celda.
 * Se activa solo si `day` no es `null`. Por defecto, una lambda vacía.
 */
data class EmotionDay(
    val day: Int?,
    @DrawableRes val emotionRes: Int?, // Anotación para indicar que es un ID de Drawable
    val isSelected: Boolean = false, // No seleccionado por defecto
    val onClick: () -> Unit = {} // Acción vacía por defecto
)