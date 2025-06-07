/* ui/root/TibiBalanceRoot.kt */
package com.app.tibibalance.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.ui.navigation.AppNavGraph
import com.app.tibibalance.ui.theme.AppThemeViewModel
import com.app.tibibalance.ui.theme.TibiBalanceTheme
import com.app.tibibalance.ui.tutorial.TutorialOverlay
import com.app.tibibalance.ui.tutorial.TutorialViewModel
import com.app.tibibalance.ui.tutorial.tutorialTarget

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TibiBalanceRoot() {
    val themeVm: AppThemeViewModel = hiltViewModel()
    val tutorialVm: TutorialViewModel = hiltViewModel()
    val mode   = themeVm.mode.collectAsState().value
    val step   = tutorialVm.currentStep.collectAsState().value

    TibiBalanceTheme(mode = mode) {
        AppNavGraph()
        TutorialOverlay(step = step, onNext = tutorialVm::proceedToNextStep, onSkip = tutorialVm::skipTutorial)
    }
}
