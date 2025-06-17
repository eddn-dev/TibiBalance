/* ui/screens/home/HomeScreen.kt */
package com.app.tibibalance.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.tutorial.TutorialOverlay
import com.app.tibibalance.tutorial.TutorialViewModel
import com.app.tibibalance.tutorial.rememberTutorialTarget
import com.app.tibibalance.ui.components.containers.ConnectWatchCard
import com.app.tibibalance.ui.components.containers.HealthPermissionsCard
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.utils.Centered
import com.app.tibibalance.ui.components.utils.PagerIndicator
import com.app.tibibalance.ui.permissions.HEALTH_CONNECT_READ_PERMISSIONS
import com.app.tibibalance.ui.permissions.rememberHealthPermissionLauncher
import com.app.tibibalance.ui.screens.home.activities.ActivityFeed
import com.app.tibibalance.ui.screens.home.activities.ActivityLogDialog
import com.app.tibibalance.ui.components.containers.DailyTip as DailyTipContainer

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()


) {

    val context = LocalContext.current
    val hcClient = remember { HealthConnectClient.getOrCreate(context) }      // se cae al impl def.  :contentReference[oaicite:3]{index=3}

    val launcher = rememberHealthPermissionLauncher(hcClient) { granted ->
        viewModel.onPermissionsResult(granted)
    }
    val tutorialVm: TutorialViewModel = hiltViewModel()
    val state by viewModel.ui.collectAsState()

    // Flags de conveniencia
    val metricsEnabled  = state.hcAvailable
    val needsPerms      = metricsEnabled && !state.healthPermsGranted

    // Páginas dinámicas: Si no hay HC => 1 página, si sí => 2
    val pageCount = if (metricsEnabled) 2 else 1
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

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

    // Corrige scroll si el numero de páginas cambia
    LaunchedEffect(pageCount) {
        if(pagerState.currentPage >= pageCount) pagerState.animateScrollToPage(0)
    }

    // Al entrar, refresca permisos (por si el usuario vuelve de ajustes)
    LaunchedEffect(Unit) { viewModel.refreshHealthPermissions() }

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
                state       = pagerState,
                pageSpacing = 16.dp,
                modifier    = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> DailyTipPage(state, tutorialVm, currentTarget)
                    1 -> MetricsPage(
                        hasPermissions = state.healthPermsGranted,
                        onGrantPerms   = { launcher.launch(HEALTH_CONNECT_READ_PERMISSIONS) },
                        onInstallHc    = { /* ir a Play Store */ },
                        tutorialVm     = tutorialVm,
                        currentTarget  = currentTarget
                    )
                }
            }

            PagerIndicator(
                pagerState  = pagerState,
                pageCount   = pageCount,
                modifier    = Modifier.align(Alignment.CenterHorizontally)
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

@Composable
private fun DailyTipPage(
    state: HomeUi,
    tutorialVm: TutorialViewModel,
    currentTarget: String?,
) {
    state.dailyTip?.let { tip ->
        DailyTipContainer(
            tip      = tip,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_tip_card")
                .then(
                    rememberTutorialTarget(
                        targetId       = "daily_tip_card",
                        currentTargetId = currentTarget,
                        onPositioned   = tutorialVm::updateTargetBounds
                    )
                )
        )
    } ?: Centered("Sin tip disponible")
}

@Composable
private fun MetricsPage(
    hasPermissions: Boolean,
    onGrantPerms: () -> Unit,
    onInstallHc: () -> Unit,
    tutorialVm: TutorialViewModel,
    currentTarget: String?
) {

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
            .then(
                rememberTutorialTarget(
                    targetId        = "stats_graph",
                    currentTargetId = currentTarget,
                    onPositioned    = tutorialVm::updateTargetBounds
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Title("Métricas")

        when {
            !hasPermissions ->
                HealthPermissionsCard(
                    onGrantClick = onGrantPerms,
                    modifier     = Modifier.fillMaxWidth()
                )
            else -> {
                // Aquí irá tu Dashboard real
                ConnectWatchCard(
                    onClick  = { /* TODO */ },
                    modifier = Modifier.then(
                        rememberTutorialTarget(
                            targetId        = "connect_watch_card",
                            currentTargetId = currentTarget,
                            onPositioned    = tutorialVm::updateTargetBounds
                        )
                    )
                )
            }
        }
    }
}
