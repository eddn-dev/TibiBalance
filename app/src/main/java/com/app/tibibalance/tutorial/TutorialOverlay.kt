package com.app.tibibalance.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TutorialOverlay(
    viewModel: TutorialViewModel,
    content: @Composable () -> Unit
) {
    val step by viewModel.currentStep.collectAsState()

    if (step == null) {
        content()
        return
    }

    val s = step!!

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        val backgroundColor = Color(0x88000000)
        val dialogModifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)

        when (s.id) {
            "intro" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row {
                            Button(onClick = { viewModel.proceedToNextStep() }) {
                                Text("Comenzar")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(onClick = { viewModel.skipTutorial() }) {
                                Text("Omitir", color = Color.LightGray)
                            }
                        }
                    }
                }
            }

            "habits_tab" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 140.dp)
                            .then(dialogModifier),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.proceedToNextStep() }) {
                            Text("Siguiente")
                        }
                    }
                }
            }

            "habit_fab" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 100.dp)
                            .then(dialogModifier),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Button(onClick = {
                                viewModel.proceedToNextStep()
                            }) {
                                Text("Crear Hábito Ahora")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.skipHabitFab() }) {
                                Text("Más tarde")
                            }
                        }
                    }
                }
            }

            "daily_progress", "daily_tip", "stats" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 100.dp)
                            .then(dialogModifier),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.proceedToNextStep() }) {
                            Text("Siguiente")
                        }
                    }
                }
            }

            "activity_fab" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 100.dp, end = 16.dp)
                            .then(dialogModifier),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.proceedToNextStep() }) {
                            Text("Siguiente")
                        }
                    }
                }
            }

            "profile", "settings" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = dialogModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.proceedToNextStep() }) {
                            Text("Siguiente")
                        }
                    }
                }
            }

            "final" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = dialogModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = s.title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = s.message)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.finishTutorial() }) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}
