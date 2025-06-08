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
import com.app.tibibalance.tutorial.TutorialOverlay
import com.app.tibibalance.tutorial.TutorialViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TibiBalanceRoot() {
    val themeVm: AppThemeViewModel = hiltViewModel()
    val tutorialVm: TutorialViewModel = hiltViewModel()
    val mode   = themeVm.mode.collectAsState().value

    TibiBalanceTheme(mode = mode) {
        TutorialOverlay(viewModel = tutorialVm) {
            AppNavGraph()
        }
    }
}
