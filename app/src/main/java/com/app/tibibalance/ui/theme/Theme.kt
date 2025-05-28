package com.app.tibibalance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.app.domain.enums.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = White,
    primaryContainer = BluePrimaryLight,
    onPrimaryContainer = White,
    background = SurfaceDark,
    surface = SurfaceDark,
    onBackground = White,
    onSurface = White,
    secondary = BluePrimaryLight,
    tertiary = BluePrimaryDark,
    error = Alert
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = White,
    primaryContainer = BluePrimaryDark,
    onPrimaryContainer = White,
    background = White,
    surface = SurfaceLight,
    onBackground = Color.Black,
    onSurface = Color.Black,
    secondary = BluePrimaryLight,
    tertiary = BluePrimaryDark,
    error = Alert,
)

@Composable
fun TibiBalanceTheme(
    mode: ThemeMode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

