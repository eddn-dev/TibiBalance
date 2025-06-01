/* :app/ui/screens/home/HomeScreen.kt */
package com.app.tibibalance.ui.screens.home

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
import com.app.tibibalance.ui.components.utils.PagerIndicator

private const val PAGES = 2     // Tip · Métricas

/* :app/ui/screens/home/HomeScreen.kt */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state      by viewModel.ui.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /* ── Saludo siempre visible ─────────────────────────── */
        val name = state.user?.displayName.orEmpty()
        Title("¡Hola de nuevo! $name")

        /* ── Pager con Tip / Métricas ───────────────────────── */
        HorizontalPager(
            state   = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> TipPage(
                    tip       = state.dailyTip
                )
                1 -> MetricsPage()
            }
        }

        /* ── Indicador inferior ─────────────────────────────── */
        PagerIndicator(
            pagerState = pagerState,
            pageCount  = 2,
            modifier   = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/* -------------- Página 0: Tip del día ---------------- */
@Composable
private fun TipPage(
    tip: DailyTip?
) {
    if (tip != null) {
        DailyTip(tip = tip)
    } else {
        // Fallback cuando no hay tip disponible (raro)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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