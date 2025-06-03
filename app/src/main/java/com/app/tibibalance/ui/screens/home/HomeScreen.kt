/* :app/ui/screens/home/HomeScreen.kt */
package com.app.tibibalance.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.entities.DailyTip
import com.app.tibibalance.ui.components.containers.DailyTip
import com.app.tibibalance.ui.components.texts.Title // drawables / colores
import com.app.tibibalance.ui.components.containers.ConnectWatchCard
import com.app.tibibalance.ui.screens.home.activities.ActivityFeed
import com.app.tibibalance.ui.components.utils.PagerIndicator
import com.app.tibibalance.ui.screens.home.activities.ActivityLogDialog

private const val PAGES = 2     // Tip · Métricas

/* :app/ui/screens/home/HomeScreen.kt */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state      by viewModel.ui.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /* Saludo */
        Title("¡Hola de nuevo! ${state.user?.displayName.orEmpty()}")

        /* Pager reducido */
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> TipPage(state.dailyTip)
                1 -> MetricsPage()
            }
        }

        PagerIndicator(
            pagerState = pagerState,
            pageCount  = 2,
            modifier   = Modifier.align(Alignment.CenterHorizontally)
        )

        /* -------- Feed de actividades -------- */
        ActivityFeed(
            modifier   = Modifier
                .weight(1f)
                .fillMaxWidth(),
            activities = state.activities,
            onClickCard = viewModel::openLog            // ✅ abre el modal
        )


        /* -------- diálogo de registro -------- */
        state.selectedActivity?.let { sel ->
            ActivityLogDialog(
                ui        = sel,
                onDismiss = { viewModel.dismissLog() },
                onConfirm = { qty, st -> viewModel.saveProgress(sel.act.id, qty, st) }
            )
        }
    }
}


/* -------------- Página 0: Tip del día ---------------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TipPage(
    tip: DailyTip?
) {
    if (tip != null) {
        DailyTip(tip = tip)
    } else {
        // Fallback cuando no hay tip disponible (raro)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Text("Sin tip disponible", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/* -------------- Página 1: Métricas ------------------- */
@Composable
private fun MetricsPage() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Title("Métricas")      // encabezado grande

        /* Invitación a conectar reloj.
           ⚠️ Aún NO depende de isWatchConnected; se mostrará siempre.
           Cuando implementes la lógica, ocúltalo cuando `watchConnected == true`. */
        ConnectWatchCard(
            onClick = { /* TODO: navegar a flujo de enlace */ }
        )
    }
}