/*package com.app.tibibalance.tutorial

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.psoffritti.taptargetcompose.TapTargetCoordinator
import com.psoffritti.taptargetcompose.TapTargetDefinition
import com.psoffritti.taptargetcompose.TextDefinition
import com.psoffritti.taptargetcompose.tapTarget

/**
 * Overlay composable displaying the current tutorial step using TapTargetCompose.
 */
@Composable
fun TutorialOverlay(viewModel: TutorialViewModel, content: @Composable () -> Unit) {
    val step by viewModel.currentStep.collectAsState()

    val show = step != null

    TapTargetCoordinator(showTapTargets = show, onComplete = { viewModel.proceedToNextStep() }) {
        content()
        step?.let { s ->
            val definition = TapTargetDefinition(
                title = TextDefinition(s.title),
                description = TextDefinition(s.message),
                precedence = 0
            )
            Button(onClick = { viewModel.proceedToNextStep() }, modifier = Modifier.tapTarget(definition)) {
                Text(" ")
            }
        }
    }
}
*/