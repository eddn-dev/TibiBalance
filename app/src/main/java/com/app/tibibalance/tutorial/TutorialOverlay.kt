package com.app.tibibalance.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.psoffritti.taptargetcompose.TapTargetCoordinator

/**
 * Overlay composable displaying the current tutorial step using TapTargetCompose.
 */
@Composable
fun TutorialOverlay(viewModel: TutorialViewModel, content: @Composable () -> Unit) {
    val step by viewModel.currentStep.collectAsState()

    TapTargetCoordinator(showTapTargets = step != null, onComplete = { viewModel.proceedToNextStep() }) {
        content()

        if (step != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(step!!.title)
                    Text(step!!.message)
                    Row(Modifier.padding(top = 8.dp)) {
                        Button(onClick = { viewModel.skipTutorial() }) {
                            Text("Omitir")
                        }
                        Button(onClick = { viewModel.proceedToNextStep() }, modifier = Modifier.padding(start = 8.dp)) {
                            Text("Siguiente")
                        }
                    }
                }
            }
        }
    }
}
