/**
 * @file      EmotionalCalendarScreen.kt
 * @ingroup   ui_screens_emotional
 * @brief     Pantalla de calendario emocional con flujo declarativo (StateFlow + DialogState).
 *
 * Este archivo define la pantalla principal `EmotionalCalendarScreen`, la cual:
 *  - Observa el estado de la UI y muestra un calendario o mensajes de carga/errores.
 *  - Maneja distintos tipos de diálogos (información, error, registro de emoción).
 *  - Incluye un enlace de texto “SABER MÁS” que despliega un modal de información adicional
 *    con texto y una imagen, cuando el usuario lo pulsa.
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.HabitContainer
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.utils.CalendarGrid
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.EmotionDay
import androidx.compose.ui.text.style.TextAlign
import com.app.tibibalance.ui.components.utils.gradient
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/* -------------------------------------------------------------------------- */
/*  Pantalla principal                                                         */
/* -------------------------------------------------------------------------- */

/**
 * @brief Pantalla principal del calendario emocional.
 *
 * Observa el estado desde el ViewModel y muestra:
 *  - Una vista de carga si está en estado Loading.
 *  - Un mensaje "Sin registros" si está vacío.
 *  - El contenido del calendario en estado Loaded.
 *  - Un mensaje de error si ocurre algún error.
 *
 * También maneja distintos diálogos de información, error o registro de emoción,
 * según el estado `DialogState` proporcionado por el ViewModel.
 *
 * @param vm Instancia inyectada de EmotionalCalendarViewModel.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmotionalCalendarScreen(
    vm: EmotionalCalendarViewModel = hiltViewModel(),
    tutorialVm: TutorialViewModel = hiltViewModel()
) {
    /* ------------ state ------------- */
    val uiState by vm.ui.collectAsState()
    val dialog by vm.dialog.collectAsState()

    /* ------------ fondo ------------- */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient()),
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.material3.IconButton(
            onClick = tutorialVm::restartTutorial,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            androidx.compose.material3.Icon(Icons.Default.Info, contentDescription = "Ayuda")
        }

        /* ---------- contenido ---------- */
        when (uiState) {
            EmotionalUiState.Loading -> Centered("Cargando…")
            EmotionalUiState.Empty -> Centered("Sin registros")
            is EmotionalUiState.Loaded -> CalendarContent(
                days = (uiState as EmotionalUiState.Loaded).days,
                onClick = vm::onCalendarDayClicked
            )
            is EmotionalUiState.Error -> Centered("Error :(")
        }

        /* ---------- diálogos desde ViewModel ---------- */
        when (dialog) {
            DialogState.None -> Unit

            is DialogState.Info -> InfoModal(
                msg = (dialog as DialogState.Info).msg,
                onDismiss = vm::dismissDialog
            )

            is DialogState.Error -> ErrorModal(
                msg = (dialog as DialogState.Error).msg,
                onDismiss = vm::dismissDialog
            )

            is DialogState.Register -> {
                val date = (dialog as DialogState.Register).date
                RegisterEmotionalStateModal(
                    date = date,
                    onDismiss = vm::dismissDialog,
                    onConfirm = { emo -> vm.confirmEmotion(date, emo) }
                )
            }
        }
        val logroFeliz by vm.logroFeliz.collectAsState()
        logroFeliz?.let { logro ->
            val iconRes = when (logro.id) {
                "feliz_7_dias"         -> R.drawable.ic_tibio_calendar
                "emociones_30_dias" -> R.drawable.ic_emocional
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

/**
 * @brief Contenido principal que muestra el calendario y un enlace "SABER MÁS".
 *
 * - Muestra el mes actual (nombre y año).
 * - Renderiza la cuadrícula de días (CalendarGrid) con los íconos emocionales.
 * - Incluye el componente `TextButtonLink` para desplegar un modal de información adicional.
 * - Muestra estadísticas rápidas de la emoción más repetida.
 *
 * Para manejar el modal “SABER MÁS”, se utiliza un estado local `showMore`.
 * Al pulsar el enlace, `showMore` cambia a true y se muestra un modal personalizado
 * con texto introductorio, una imagen y texto adicional.
 *
 * @param days   Lista de objetos EmotionDayUi que describe cada día del mes.
 * @param onClick Callback que se invoca cuando se selecciona un día del calendario.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarContent(
    days: List<EmotionDayUi>,
    onClick: (EmotionDayUi) -> Unit
) {
    // Estado local para controlar visibilidad del modal “SABER MÁS”
    var showMore by remember { mutableStateOf(false) }

    // Determinar el mes actual en formato “NombreMes Año”
    val today = LocalDate.now()
    val monthTitle = today.month
        .getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.titlecase(Locale.ROOT) } + " ${today.year}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /* Título mes */
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge
        )

        /* Calendario */
        CalendarGrid(
            month = "", // ya mostramos el título arriba
            days = days.map { dUi ->
                EmotionDay(
                    day = dUi.day,
                    emotionRes = dUi.iconRes,
                    onClick = { onClick(dUi) },
                    isSelected = false
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        /* Enlace “SABER MÁS” */
        TextButtonLink(
            text = "SABER MÁS",
            onClick = {
                showMore = true
            }
        )

        /* Estadística rápida */
        EmotionStats(days)
    }

    // ----------------- Modal personalizado “SABER MÁS” -----------------
    if (showMore) {
        ModalContainer(
            onDismissRequest = { showMore = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally // Mantener esto para el Column
            ) {
                Text(
                    fontWeight = FontWeight.SemiBold,
                    text = "¿Cómo te sientes hoy? 🤔",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Este espacio es para registrar \ntus emociones cada día. 💖",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                ImageContainer(
                    resId = R.drawable.ic_tibio_thoughtful,
                    contentDescription = "Tibio pensativo",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Toca el recuadro de hoy para empezar \na registrar tus emociones. " +
                            "\n¡La constancia diaria te ayudará a \nconocer mejor cómo te sientes! 📊",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showMore = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}

/**
 * @brief Muestra la estadística de la emoción más repetida.
 *
 * Calcula cuál fue la emoción con más frecuencia y muestra:
 *  - El ícono de dicha emoción.
 *  - El texto “Has estado {nombreEmoción} durante {número} días”.
 *
 * @param days Lista de objetos EmotionDayUi que contiene la emoción asignada a cada día.
 */
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
        text = "Estado emocional más repetido",
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
        text = "Has estado $name durante $count días",
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    )
}

/* -------------------------------------------------------------------------- */
/*  Modales reutilizables                                                      */
/* -------------------------------------------------------------------------- */

/**
 * @brief Muestra un modal de información simple (ícono de info y un texto).
 *
 * @param msg       Mensaje a mostrar como título del modal.
 * @param onDismiss Callback que se invoca al cerrar el modal.
 */
@Composable
private fun InfoModal(msg: String, onDismiss: () -> Unit) =
    ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Info,
        title = msg,
        message = "",
        primaryButton = DialogButton("Entendido", onDismiss)
    )

/**
 * @brief Muestra un modal de error (ícono de error y texto).
 *
 * @param msg       Mensaje de error a mostrar.
 * @param onDismiss Callback que se invoca al cerrar el modal.
 */
@Composable
private fun ErrorModal(msg: String, onDismiss: () -> Unit) =
    ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Error,
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        iconBgColor = MaterialTheme.colorScheme.errorContainer,
        title = "Error",
        message = msg,
        primaryButton = DialogButton("Aceptar", onDismiss)
    )

/* -------------------------------------------------------------------------- */
/*  Utils                                                                      */
/* -------------------------------------------------------------------------- */

/**
 * @brief Convierte el recurso drawable de emoción a su nombre descriptivo.
 *
 * @param resId ID de recurso drawable que representa la emoción.
 * @return Nombre de la emoción en texto, según el recurso.
 */
private fun emotionName(@DrawableRes resId: Int?): String = when (resId) {
    R.drawable.ic_happyimage      -> "Feliz"
    R.drawable.ic_sadimage        -> "Triste"
    R.drawable.ic_calmimage       -> "Tranquilo"
    R.drawable.ic_angryimage      -> "Enojado"
    R.drawable.ic_disgustingimage -> "Disgustado"
    R.drawable.ic_fearimage       -> "Asustado"
    else                          -> ""
}

