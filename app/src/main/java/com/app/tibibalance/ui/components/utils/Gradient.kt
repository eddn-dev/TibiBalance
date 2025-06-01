package com.app.tibibalance.ui.components.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

@Composable
fun gradient(): Brush {
        return Brush.verticalGradient( // aplica los colores background y surface
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surface
            )
        )
}