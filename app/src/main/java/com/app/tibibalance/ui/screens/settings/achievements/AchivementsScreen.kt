/* ui/screens/settings/achievements/AchievementsScreen.kt */
package com.app.tibibalance.ui.screens.settings.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.containers.AchievementContainer
import com.app.tibibalance.ui.components.containers.FormContainer
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.utils.gradient

/** Descripción estática de cada logro para dibujar icono, texto, etc. */
private data class AchvUi(
    val id: String,
    val icon: Int,
    val title: String,
    val desc: String
)

/* --------------- lista master (en el orden deseado) ---------------- */
private val allUi = listOf(
    AchvUi("foto_perfil",        R.drawable.ic_tibio_camera     , "Un placer conocernos",  "Cambia tu foto de perfil."),
    AchvUi("tibio_salud",        R.drawable.ic_tibio_salud      , "Tibio saludable",       "Agrega un hábito de salud."),
    AchvUi("tibio_productividad",R.drawable.ic_tibio_productivo , "Tibio productivo",      "Agrega un hábito de productividad."),
    AchvUi("tibio_bienestar",    R.drawable.ic_tibio_bienestar  , "Tibio del bienestar",   "Agrega un hábito de bienestar."),
    AchvUi("primer_habito",      R.drawable.ic_tibio_explorer   , "El inicio del reto",    "Agrega tu primer hábito con modo reto activado."),
    AchvUi("cinco_habitos",      R.drawable.ic_tibio_arquitecto , "La sendera del reto",   "Agrega cinco hábitos con modo reto activado."),
    AchvUi("feliz_7_dias",       R.drawable.ic_tibio_calendar   , "Todo en su lugar",      "Siete días seguidos feliz."),
    AchvUi("emociones_30_dias",  R.drawable.ic_emocional        , "Un tibio emocional",    "Registra emociones 30 días seguidos."),
    AchvUi("noti_personalizada", R.drawable.ic_tibio_reloj      , "¡Ya es hora!",          "Personaliza tus notificaciones.")
)

/* ------------------------------------------------------------------- */
@Composable
fun AchievementsScreen(
    onNavigateUp: () -> Unit,
    vm: AchievementsViewModel = hiltViewModel()
) {
    val achMap by vm.achievements.collectAsState()

    // separa los logros dependiendo de su estado
    val (unlocked, locked) = remember(achMap) {
        allUi.partition { achMap[it.id]?.unlocked == true }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient())
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(77.dp))

            FormContainer(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {

                SectionTitle("Logros desbloqueados")
                if (unlocked.isEmpty()) {
                    Subtitle("Todavía no has desbloqueado ninguno.")
                } else {
                    unlocked.forEach { ui -> DrawItem(ui, achMap[ui.id]) }
                }

                Spacer(Modifier.height(24.dp))

                SectionTitle("Logros pendientes")
                locked.forEach { ui -> DrawItem(ui, achMap[ui.id]) }
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

/* --------------------- UI helpers ---------------------------------- */
@Composable private fun SectionTitle(text: String) = Text(
    text,
    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    color = MaterialTheme.colorScheme.onSurface,
    textAlign = TextAlign.Center,
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
)

/** Texto gris de apoyo. */
@Composable private fun Subtitle(text: String) = Text(
    text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth()
)

/** Dibuja un logro individual con su progreso. */
@Composable
private fun DrawItem(ui: AchvUi, ach: com.app.domain.entities.Achievement?) =
    AchievementContainer(
        iconRes     = ui.icon,
        title       = ui.title,
        description = ui.desc,
        percent     = ach?.progress ?: 0,
        isUnlocked  = ach?.unlocked == true
    )
