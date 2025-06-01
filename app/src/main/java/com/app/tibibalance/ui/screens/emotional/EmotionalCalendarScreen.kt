/**
 * @file      EmotionalCalendarScreen.kt
 * @ingroup   ui_screens_emotional
 * @brief     Pantalla de calendario emocional con flujo declarativo (StateFlow + DialogState).
 *
 * @see EmotionalCalendarViewModel
 * @see CalendarGrid
 */
package com.app.tibibalance.ui.screens.emotional

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.containers.HabitContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.utils.CalendarGrid
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.EmotionDay
import com.app.tibibalance.ui.theme.gradient
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/* -------------------------------------------------------------------------- */
/*  Pantalla principal                                                         */
/* -------------------------------------------------------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmotionalCalendarScreen(
    vm: EmotionalCalendarViewModel = hiltViewModel()
) {
    /* ------------ state ------------- */
    val uiState   by vm.ui.collectAsState()
    val dialog    by vm.dialog.collectAsState()

    /* ------------ fondo ------------- */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.TopCenter
    ) {

        /* ---------- contenido ---------- */
        when (uiState) {
            EmotionalUiState.Loading -> Centered("Cargando…")
            EmotionalUiState.Empty   -> Centered("Sin registros")
            is EmotionalUiState.Loaded -> CalendarContent(
                days    = (uiState as EmotionalUiState.Loaded).days,
                onClick = vm::onCalendarDayClicked
            )
            is EmotionalUiState.Error -> Centered("Error :(")
        }

        /* ---------- diálogos ---------- */
        when (dialog) {
            DialogState.None -> Unit

            is DialogState.Info -> InfoModal(
                msg       = (dialog as DialogState.Info).msg,
                onDismiss = vm::dismissDialog
            )

            is DialogState.Error -> ErrorModal(
                msg       = (dialog as DialogState.Error).msg,
                onDismiss = vm::dismissDialog
            )

            is DialogState.Register -> {
                val date = (dialog as DialogState.Register).date
                RegisterEmotionalStateModal(
                    date      = date,
                    onDismiss = vm::dismissDialog,
                    onConfirm = { emo -> vm.confirmEmotion(date, emo) }
                )
            }
        }
        val logroFeliz by vm.logroFeliz.collectAsState()
        logroFeliz?.let { logro ->
            val iconRes = when (logro.id) {
                "feliz_7_dias"         -> R.drawable.calendar
                "emociones_30_dias" -> R.drawable.emocional
                else                  -> R.drawable.avatar_placeholder
            }
            ModalAchievementDialog(
                visible = true,
                iconResId = iconRes,
                title = "¡Logro desbloqueado!",
                message = "${logro.name}\n${logro.description}",
                primaryButton = DialogButton("Aceptar") {
                    vm.ocultarLogroFeliz()
                }
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  Sub-UI helpers                                                             */
/* -------------------------------------------------------------------------- */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarContent(
    days   : List<EmotionDayUi>,
    onClick: (EmotionDayUi) -> Unit
) {
    val today      = LocalDate.now()
    val monthTitle = today.month.getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.titlecase(Locale.ROOT) } + " ${today.year}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /* Título mes */
        Text(monthTitle, style = MaterialTheme.typography.titleLarge)

        /* Calendario */
        CalendarGrid(
            month = "",          // título ya se muestra arriba
            days  = days.map { dUi ->
                EmotionDay(
                    day        = dUi.day,
                    emotionRes = dUi.iconRes,
                    onClick    = { onClick(dUi) },
                    isSelected = false
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        /* Estadística rápida */
        EmotionStats(days)
    }
}

@Composable
private fun EmotionStats(days: List<EmotionDayUi>) {
    val (topIcon, count) = remember(days) {
        days
            .mapNotNull { it.iconRes }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.let { it.key to it.value }
            ?: (null to 0)
    }
    val name = emotionName(topIcon)

    Text(
        "Estado emocional más repetido",
        style = MaterialTheme.typography.titleMedium
    )
    HabitContainer(
        icon = {
            ImageContainer(
                resId = topIcon ?: R.drawable.ic_happyimage,
                contentDescription = null,
                modifier = Modifier.size(45.dp)
            )
        },
        text    = "Has estado $name durante $count días",
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    )
}

/* -------------------------------------------------------------------------- */
/*  Modales reutilizables                                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun InfoModal(msg: String, onDismiss: () -> Unit) =
    ModalInfoDialog(
        visible       = true,
        icon          = Icons.Default.Info,
        title         = msg,
        message       = "",
        primaryButton = DialogButton("Entendido", onDismiss)
    )

@Composable
private fun ErrorModal(msg: String, onDismiss: () -> Unit) =
    ModalInfoDialog(
        visible       = true,
        icon          = Icons.Default.Error,
        iconColor     = MaterialTheme.colorScheme.onErrorContainer,
        iconBgColor   = MaterialTheme.colorScheme.errorContainer,
        title         = "Error",
        message       = msg,
        primaryButton = DialogButton("Aceptar", onDismiss)
    )

/* -------------------------------------------------------------------------- */
/*  Utils                                                                      */
/* -------------------------------------------------------------------------- */

private fun emotionName(@DrawableRes resId: Int?): String = when (resId) {
    R.drawable.ic_happyimage        -> "Feliz"
    R.drawable.ic_sadimage          -> "Triste"
    R.drawable.ic_calmimage         -> "Tranquilo"
    R.drawable.ic_angryimage        -> "Enojado"
    R.drawable.ic_disgustingimage   -> "Disgustado"
    R.drawable.ic_fearimage         -> "Asustado"
    else                            -> ""
}

