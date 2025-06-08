package com.app.tibibalance.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.psoffritti.taptargetcompose.TapTargetDefinition
import com.psoffritti.taptargetcompose.TextDefinition
import com.psoffritti.taptargetcompose.tapTarget

/** Modifier extension that attaches a tap target when the current step matches. */
@Composable
fun Modifier.tutorialTarget(vm: TutorialViewModel, id: String): Modifier {
    val step by vm.currentStep.collectAsState()
    return if (step?.targetId == id) {
        val definition = TapTargetDefinition(
            title = TextDefinition(step!!.title),
            description = TextDefinition(step!!.message),
            precedence = 0
        )
        this.tapTarget(definition).then(Modifier.testTag(id))
    } else this
}
