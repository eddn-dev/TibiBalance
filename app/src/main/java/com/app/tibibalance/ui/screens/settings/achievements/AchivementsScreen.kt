package com.app.tibibalance.ui.screens.settings.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.containers.AchievementContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight


import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.app.tibibalance.ui.screens.settings.achievements.AchievementsViewModel
import androidx.compose.runtime.getValue


@Composable
fun AchievementsScreen(
    onNavigateUp: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF3EA8FE).copy(alpha = .25f), Color.White)
    )

    val vm: AchievementsViewModel = hiltViewModel()
    val logros by vm.logros.collectAsState()

    // Contenedor principal que ocupa toda la pantalla.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient) // Aplica el fondo degradado.
    ) {
        // Columna principal para el contenido, permite desplazamiento vertical.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Habilita el scroll.
                .padding(horizontal = 16.dp, vertical = 24.dp), // Padding general.
            horizontalAlignment = Alignment.CenterHorizontally // Centra elementos horizontalmente.
        ) {
            Spacer(modifier = Modifier.height(77.dp))// Espacio superior.


            FormContainer (
                backgroundColor = Color(0xFFAED3E3),
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier
                        .background(Color(0xFF5997C7), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 100.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally) // Centrado dentro del FormContainer
                ) {
                    Text(
                        text = "Tus logros",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                //Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "⬇\uFE0F Desplazate hacia abajo⬇\uFE0F\npara ver el resto de los logros.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                //Spacer(modifier = Modifier.height(20.dp))

                val fotoPerfil = logros["foto_perfil"]
                AchievementContainer(
                    iconRes = R.drawable.camera,
                    title = "Un placer conocernos",
                    description = "Cambia tu foto de perfil.",
                    percent = fotoPerfil?.progress ?: 0,
                    isUnlocked = fotoPerfil?.unlocked == true
                )

                val tibioSalud = logros["tibio_salud"]
                AchievementContainer(
                    iconRes = R.drawable.salud,
                    title = "Tibio saludable",
                    description = "Agrega un hábito de salud.",
                    percent = tibioSalud?.progress ?: 0,
                    isUnlocked = tibioSalud?.unlocked == true
                )

                val tibioProductivo = logros["tibio_productividad"]
                AchievementContainer(
                    iconRes = R.drawable.productivo,
                    title = "Tibio productivo",
                    description = "Agrega un hábito de productividad.",
                    percent = tibioProductivo?.progress ?: 0,
                    isUnlocked = tibioProductivo?.unlocked == true
                )

                val tibioBienestar = logros["tibio_bienestar"]
                AchievementContainer(
                    iconRes = R.drawable.bienestar,
                    title = "Tibio del bienestar",
                    description = "Agrega un hábito de bienestar.",
                    percent = tibioBienestar?.progress ?: 0,
                    isUnlocked = tibioBienestar?.unlocked == true
                )

                val primerHabito = logros["primer_habito"]
                AchievementContainer(
                    iconRes = R.drawable.explorer,
                    title = "El inicio del reto",
                    description = "Agrega tu primer hábito con modo reto activado.",
                    percent = primerHabito?.progress ?: 0,
                    isUnlocked = primerHabito?.unlocked == true
                )

                val cincoHabitos = logros["cinco_habitos"]
                AchievementContainer(
                    iconRes = R.drawable.arquitecto,
                    title = "La sendera del reto",
                    description = "Agrega cinco hábitos con modo reto activado.",
                    percent = cincoHabitos?.progress ?: 0,
                    isUnlocked = cincoHabitos?.unlocked == true
                )

                val feliz7Dias = logros["feliz_7_dias"]
                AchievementContainer(
                    iconRes = R.drawable.calendar,
                    title = "Todo en su lugar",
                    description = "Registra un estado de ánimo “feliz” por siete días consecutivos.",
                    percent = feliz7Dias?.progress ?: 0,
                    isUnlocked = feliz7Dias?.unlocked == true
                )

                val emotions30 = logros["emociones_30_dias"]
                AchievementContainer(
                    iconRes = R.drawable.emocional,
                    title = "Un tibio emocional",
                    description = "Registra tus emociones por 30 días consecutivos.",
                    percent = emotions30?.progress ?: 0,
                    isUnlocked = emotions30?.unlocked == true
                )

                val notiPersonalizada = logros["noti_personalizada"]
                AchievementContainer(
                    iconRes = R.drawable.reloj,
                    title = "¡Ya es hora!",
                    description = "Descubriste la personalización de notificaciones desde configuración.",
                    percent = notiPersonalizada?.progress ?: 0,
                    isUnlocked = notiPersonalizada?.unlocked == true
                )

                //Spacer(modifier = Modifier.height(120.dp))
            }

        }
        Header(
            title = "Logros y Rachas",
            showBackButton = true,
            onBackClick = onNavigateUp,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
/*@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PreviewAchievementsScreen() {
    AchievementsScreen()
}*/