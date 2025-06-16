/* ui/screens/home/HomeScreen.kt */
package com.app.tibibalance.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.entities.DailyTip
import com.app.tibibalance.ui.components.containers.ConnectWatchCard
import com.app.tibibalance.ui.components.containers.DailyTip as DailyTipContainer
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.PagerIndicator
import com.app.tibibalance.ui.components.utils.gradient
import com.app.tibibalance.ui.screens.home.activities.ActivityFeed
import com.app.tibibalance.ui.screens.home.activities.ActivityLogDialog
import com.app.tibibalance.tutorial.rememberTutorialTarget
import com.app.tibibalance.tutorial.TutorialOverlay
import com.app.tibibalance.tutorial.TutorialViewModel

private const val PAGES = 2

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val tutorialVm: TutorialViewModel = hiltViewModel()
    val state by viewModel.ui.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { PAGES })

    // 1️⃣ Tutorial de Home principal (Main section)
    LaunchedEffect(Unit) {
        tutorialVm.startHomeTutorialIfNeeded(TutorialViewModel.HomeTutorialSection.Main)
    }
    // 2️⃣ Tutorial de Métricas al cambiar a la página 1 (una sola vez)
    var statsTutorialLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 && !statsTutorialLaunched) {
            tutorialVm.startHomeTutorialIfNeeded(TutorialViewModel.HomeTutorialSection.Stats)
            statsTutorialLaunched = true
        }
    }

    // Paso actual del tutorial
    val currentStep by tutorialVm.currentStep.collectAsState()
    val currentTarget = currentStep?.targetId

    // ─── Envuelve TODO en el overlay ───────────────────────────────
    TutorialOverlay(viewModel = tutorialVm) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón de ayuda global
            IconButton(
                onClick = {
                    if (pagerState.currentPage == 1)
                        tutorialVm.restartHomeTutorial(TutorialViewModel.HomeTutorialSection.Stats)
                    else
                        tutorialVm.restartHomeTutorial(TutorialViewModel.HomeTutorialSection.Main)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Ayuda"
                )
            }

            // Saludo
            Title("¡Hola de nuevo! ${state.user?.displayName.orEmpty()}")

            // Paginador: Tip del día ↔ Métricas
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> {
                        state.dailyTip?.let { tip ->
                            DailyTipContainer(
                                tip = tip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_tip_card")
                                    .then(
                                        rememberTutorialTarget(
                                            targetId = "daily_tip_card",
                                            currentTargetId = currentTarget,
                                            onPositioned = tutorialVm::updateTargetBounds
                                        )
                                    )
                            )
                        } ?: Centered("Sin tip disponible")
                    }
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp)
                                .then(
                                    rememberTutorialTarget(
                                        targetId = "stats_graph",
                                        currentTargetId = currentTarget,
                                        onPositioned = tutorialVm::updateTargetBounds
                                    )
                                ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Title("Métricas")
                            ConnectWatchCard(
                                onClick = { /* TODO: navegar al flujo de smartwatch */ },
                                modifier = Modifier.then(
                                    rememberTutorialTarget(
                                        targetId = "connect_watch_card",
                                        currentTargetId = currentTarget,
                                        onPositioned = tutorialVm::updateTargetBounds
                                    )
                                )
                            )
                        }
                    }
                }
            }

            // Indicador de páginas
            PagerIndicator(
                pagerState = pagerState,
                pageCount = PAGES,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Feed de actividades (Paso 0 en Home Main)
            val dailyProgressModifier = rememberTutorialTarget(
                targetId = "daily_progress_card",
                currentTargetId = currentTarget,
                onPositioned = tutorialVm::updateTargetBounds
            )
            ActivityFeed(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(dailyProgressModifier),
                activities = state.activities,
                onClickCard = viewModel::openLog
            )

            // Diálogo para registrar actividad
            state.selectedActivity?.let { sel ->
                ActivityLogDialog(
                    ui = sel,
                    onDismiss = { viewModel.dismissLog() },
                    onConfirm = { qty, st -> viewModel.saveProgress(sel.act.id, qty, st) }
                )
            }
        }
    }
}
