package com.app.tibibalance.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.airbnb.lottie.compose.*
import androidx.compose.ui.unit.sp
import com.app.tibibalance.R

/**
 * Modifier to mark a composable as a tutorial target.
 * When targetId == currentTargetId, reports its bounds to the ViewModel.
 */
@Composable
fun rememberTutorialTarget(
    targetId: String,
    currentTargetId: String?,
    onPositioned: (Rect) -> Unit
): Modifier = if (targetId == currentTargetId) {
    Modifier
        .testTag(targetId)
        .onGloballyPositioned { coords ->
            val pos = coords.localToWindow(Offset.Zero)
            val size = coords.size.toSize()
            onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
        }
} else {
    Modifier.testTag(targetId)
}

/**
 * TutorialOverlay draws:
 * 1) A semi-transparent scrim over the UI
 * 2) (Optional) a highlight around a target
 * 3) A dialog or bottom sheet per TutorialLayout
 * 4) A video modal for VideoDialog steps
 */
@Composable
fun TutorialOverlay(
    viewModel: TutorialViewModel,
    content: @Composable () -> Unit
) {
    val stepData by viewModel.currentStep.collectAsState()

    // If no tutorial step is active, just render the content
    if (stepData == null) {
        content()
        return
    }
    val step = stepData!!

    // Determine scrim color based on system dark theme
    val scrimColor = if (!isSystemInDarkTheme()) {
        Color.Black.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }

    // Pick the appropriate Lottie resource for VideoDialog steps
    val lottieRes = when (step.id) {
        "habit_fab"       -> com.app.tibibalance.R.raw.habits
        "emotion_history" -> com.app.tibibalance.R.raw.emotions
        "smartwatch"      -> com.app.tibibalance.R.raw.home
        "navigation"      -> com.app.tibibalance.R.raw.nav
        "intro"           -> com.app.tibibalance.R.raw.tibio_welcome
        else              -> com.app.tibibalance.R.raw.habits
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Box(Modifier.fillMaxSize()) {
        // 1) Underlying screen content
        content()

        // 2) Scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(scrimColor)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        )

            when (step.layout) {
                TutorialLayout.CenteredIntro -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(16.dp))

                            LottieAnimation(
                                composition = rememberLottieComposition(LottieCompositionSpec.RawRes(com.app.tibibalance.R.raw.tibio_welcome)).value,
                                progress = animateLottieCompositionAsState(
                                    rememberLottieComposition(LottieCompositionSpec.RawRes(com.app.tibibalance.R.raw.tibio_welcome)).value,
                                    iterations = LottieConstants.IterateForever
                                ).value,
                                modifier = Modifier.size(220.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = step.message,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(24.dp))
                            Row {
                                Button(
                                    onClick = { viewModel.proceedToNextStep() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("Comenzar")
                                }
                                Spacer(Modifier.width(16.dp))
                                TextButton(
                                    onClick = { viewModel.finishTutorial() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("Omitir")
                                }
                            }
                        }
                    }
                }
                TutorialLayout.BottomRight -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = step.message,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.proceedToNextStep() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Siguiente")
                            }
                        }
                    }
                }
                TutorialLayout.CenteredDialog -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(24.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = step.message,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.proceedToNextStep() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Siguiente")
                            }
                        }
                    }
                }
                TutorialLayout.VideoDialog -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                step.title,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(16.dp))

                            LottieAnimation(
                                composition = composition,
                                progress = progress,
                                modifier = Modifier.size(220.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = step.message,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (viewModel.hasNextStep()) {
                                        viewModel.proceedToNextStep()
                                    } else {
                                        viewModel.finishTutorial()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (viewModel.hasNextStep()) "Siguiente" else "Entendido")
                            }
                        }
                    }
                }
                TutorialLayout.FinalMessage -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center)
                            .padding(24.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            if (step.id == "final_home") {
                                Text(
                                    text = "Puedes seguir explorando la aplicación\ny  encontrar más tutoriales.",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF00DFF7),
                                                    Color(0xFF008EFF)
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_tibio_tutorial),
                                        contentDescription = "Ayuda",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Unspecified
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "Si en algún momento deseas repasar\nestos tutoriales, puedes hacer uso\ndel botón de ayuda.",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = step.message,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.finishTutorial() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
    }
}
