package com.app.wear.presentation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.app.wear.R
import com.app.wear.presentation.viewmodel.UiEvent
import com.app.wear.presentation.viewmodel.WearMetricsViewModel

@Composable
fun WearAppScreen(vm: WearMetricsViewModel = hiltViewModel()) {

    val state by vm.state.collectAsState()
    val context = LocalContext.current

    /* ─── One-shot events ─── */
    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            if (ev is UiEvent.Toast) {
                Toast.makeText(context, ev.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .45f), Color.White)
    )

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* Cabecera */
        item {
            Image(
                painter = painterResource(R.drawable.tibiowatchimage),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
        item {
            Text(
                "TibiBalance Watch",
                style  = MaterialTheme.typography.title3,
                color  = MaterialTheme.colors.primary
            )
        }

        /* ─── Body ─── */
        when {
            state.isLoading -> {
                item { CircularProgressIndicator() }
                item { Text("Cargando…", textAlign = TextAlign.Center) }
            }

            state.error != null -> {
                item {
                    Text(
                        "Error: ${state.error!!.message ?: "desconocido"}",
                        color = MaterialTheme.colors.error
                    )
                }
                item {
                    Button(onClick = vm::onSendMetricsClicked) { Text("Reintentar") }
                }
            }

            else -> {   /* Success */
                item { Text("Pasos: ${state.steps}", color = MaterialTheme.colors.primary) }
                item {
                    Text(
                        "Ritmo: ${state.heartRate ?: "N/A"}",
                        color = MaterialTheme.colors.primary
                    )
                }
                item { Button(onClick = vm::onSendMetricsClicked) { Text("Enviar") } }

                /* Mensaje posterior al envío (si existe) */
                state.lastSentStatus?.takeIf { it.isNotEmpty() }?.let { status ->
                    item { Text(status, style = MaterialTheme.typography.caption2) }
                }
            }
        }
    }
}
