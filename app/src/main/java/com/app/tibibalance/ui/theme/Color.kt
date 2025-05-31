package com.app.tibibalance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BluePrimaryLight = Color(0xFF3EA8FE)

/** Darker variant of [BluePrimaryLight] used in containers */
val BluePrimaryDark = Color(0xFF0A66D9)

/** Neutral surface color for cards and light backgrounds */
val SurfaceLight = Color(0xFFECECEC)

/** Base white used across the UI */
val White = Color(0xFFFFFFFF)

/** Surface color for dark theme elements */
val SurfaceDark = Color(0xFF000B4D)

/** Gradiente de fondo*/
/* val gradient = Brush.verticalGradient(
    listOf(MaterialTheme.colorScheme.primary.copy(alpha = .25f), MaterialTheme.colorScheme.background)
) */

@Composable
fun gradient(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.verticalGradient(
            listOf(DarkPrimary, White)
        )
    } else {
        Brush.verticalGradient(
            listOf(BluePrimaryLight.copy(alpha = 0.25f), White)
        )
    }
}

/** Colores de botones*/
val Alert = Color(0xFFFF3333)
val Tint = Color.Black
val RoundedButton = Color(0xFF42A5F5)
val Border = Color.LightGray

/** Colores de textos*/
val Text = Color.Black
val TextLight = Color.DarkGray
val DefaultTint = Color.White
val LinkText = Color(0xFF007AFF)

/** Colores de contenedores*/
val Container = Color(0xFFF5FBFD)
val Form =  Color(0xFFC8DEFA)
val Tips = Color(0xFFB2E1F5)
val DailyTip = Color(0xFFDCEBF2)

/** Colores de Calendario Emocional*/
val NumberDay = Color.White.copy(alpha = 0.8f)
val DayContainerSelected = Color(0xFF85C3DE)
val DayContainerUnSelected = Color(0xCCAED3E3)

/** Colores de Barra de progreso*/
val barColor = Color(0xFFBCE2C2) // Color verde claro por defecto para la barra
val trackColor = Color(0xFFE0E0E0) // Color gris claro por defecto para el fondo

/** Inicio */
val Metrics =  Color(0xFFE0E0E0)

/** Configuracion */
val AccountSettings = Color(0xFFD8EAF1)
val PreferencesSettings = Color(0xFFE8F2F8)
val LegalSettings = Color(0xFFF3F6F8)


val DarkPrimary = Color(0xFF20304A)
val DarkPrimaryContainer = Color(0xFF546586)
val DarkSurface = Color(0xFF8E90A7)
val DarkOnSurface = Color(0xFFE0D9E0)
val DarkError = Color(0xFFFF4D4D)
