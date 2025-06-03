package com.app.tibibalance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

@Composable
fun themedImageAlpha(): Float {
    val background = MaterialTheme.colorScheme.background
    return if (background.luminance() < 0.5f) 0.8f else 1f
}