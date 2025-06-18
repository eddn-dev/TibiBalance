package com.app.tibibalance.ui.screens.emotional

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.TextButtonLink
import com.app.tibibalance.ui.components.containers.*
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalAchievementDialog
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.utils.*
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.ui.screens.settings.achievements.AchievementUnlocked
import com.app.tibibalance.tutorial.TutorialOverlay
import com.app.tibibalance.tutorial.TutorialViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmotionalCalendarScreen(
    vm: EmotionalCalendarViewModel = hiltViewModel(),
    tutorialVm: TutorialViewModel = hiltViewModel()
) {
    val uiState by vm.ui.collectAsState()
    val dialog by vm.dialog.collectAsState()
    var pendingAch by remember { mutableStateOf<AchievementUnlocked?>(null) }

    LaunchedEffect(Unit) {
        vm.unlocked.collect { pendingAch = it }
    }

    val alreadyStarted = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!alreadyStarted.value) {
            tutorialVm.startTutorialIfNeeded(Screen.Emotions)
            alreadyStarted.value = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient())
    ) {
        // Botón de ayuda para reiniciar el tutorial
        IconButton(
            onClick = { tutorialVm.restartTutorial(Screen.Emotions) },
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd)
                .background(brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00DFF7),
                        Color(0xFF008EFF)
                    )
                ),
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tibio_tutorial),
                contentDescription = "Ayuda",
                modifier = Modifier.size(90.dp)
            )
        }

        // Contenido de la pantalla
        when (uiState) {
            EmotionalUiState.Loading -> Centered("Cargando…")
            EmotionalUiState.Empty -> Centered("Sin registros")
            is EmotionalUiState.Loaded -> CalendarContent(
                days = (uiState as EmotionalUiState.Loaded).days,
                onClick = vm::onCalendarDayClicked
            )
            is EmotionalUiState.Error -> Centered("Error :(")
        }

        // Diálogos
        when (dialog) {
            DialogState.None -> Unit
            is DialogState.Info -> InfoModal((dialog as DialogState.Info).msg, vm::dismissDialog)
            is DialogState.Error -> ErrorModal((dialog as DialogState.Error).msg, vm::dismissDialog)
            is DialogState.Register -> {
                val date = (dialog as DialogState.Register).date
                RegisterEmotionalStateModal(
                    date = date,
                    onDismiss = vm::dismissDialog,
                    onConfirm = { emo -> vm.confirmEmotion(date, emo) }
                )
            }
        }

        // Logro desbloqueado
        pendingAch?.let { ach ->
            val iconRes = when (ach.id) {
                "feliz_7_dias"      -> R.drawable.ic_tibio_calendar
                "emociones_30_dias" -> R.drawable.ic_emocional
                else                -> R.drawable.avatar_placeholder
            }
            ModalAchievementDialog(
                visible = true,
                iconResId = iconRes,
                title = "¡Logro desbloqueado!",
                message = "${ach.name}\n${ach.description}",
                primaryButton = DialogButton("Aceptar") {
                    pendingAch = null
                }
            )
        }
    }

    // Overlay de tutorial
    TutorialOverlay(viewModel = tutorialVm) {}
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarContent(
    days: List<EmotionDayUi>,
    onClick: (EmotionDayUi) -> Unit
) {
    var showMore by remember { mutableStateOf(false) }

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
        Text(text = monthTitle, style = MaterialTheme.typography.titleLarge)

        CalendarGrid(
            month = "",
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

        TextButtonLink(text = "SABER MÁS", onClick = { showMore = true })
        EmotionStats(days)
    }

    if (showMore) {
        ModalContainer(onDismissRequest = { showMore = false }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
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

@Composable
private fun EmotionStats(days: List<EmotionDayUi>) {
    val (topIcon, count) = remember(days) {
        days.mapNotNull { it.iconRes }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.let { it.key to it.value }
            ?: (null to 0)
    }
    val name = emotionName(topIcon)

    Text(text = "Estado emocional más repetido", style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun InfoModal(msg: String, onDismiss: () -> Unit) =
    ModalInfoDialog(
        visible = true,
        icon = Icons.Default.Info,
        title = msg,
        message = "",
        primaryButton = DialogButton("Entendido", onDismiss)
    )

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

private fun emotionName(@DrawableRes resId: Int?): String = when (resId) {
    R.drawable.ic_happyimage      -> "Feliz"
    R.drawable.ic_sadimage        -> "Triste"
    R.drawable.ic_calmimage       -> "Tranquilo"
    R.drawable.ic_angryimage      -> "Enojado"
    R.drawable.ic_disgustingimage -> "Disgustado"
    R.drawable.ic_fearimage       -> "Asustado"
    else                          -> ""
}
