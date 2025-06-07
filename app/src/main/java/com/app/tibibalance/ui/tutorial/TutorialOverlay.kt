package com.app.tibibalance.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.psoffritti.taptargetcompose.tapTarget
import com.psoffritti.taptargetcompose.TapTargetDefinition
import com.psoffritti.taptargetcompose.TextDefinition
import com.psoffritti.taptargetcompose.TapTargetStyle

@Composable
fun TutorialOverlay(
    step: TutorialStepData?,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (step == null) return

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(step.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(step.message, style = MaterialTheme.typography.bodyMedium)
            Row {
                Button(onClick = onNext) { Text("Siguiente") }
                Button(onClick = onSkip) { Text("Omitir") }
            }
        }
    }
}

fun Modifier.tutorialTarget(current: TutorialStepData?, id: String): Modifier {
    return if (current != null && current.targetId == id) {
        this.testTag(id).tapTarget(
            TapTargetDefinition(
                precedence = 0,
                title = TextDefinition(current.title),
                description = TextDefinition(current.message),
                tapTargetStyle = TapTargetStyle()
            )
        )
    } else this
}
