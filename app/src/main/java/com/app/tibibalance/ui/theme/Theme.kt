package com.app.tibibalance.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.app.domain.enums.ThemeMode

/**
 * Paleta oscura – roles Material 3.
 */
private val DarkColorScheme = darkColorScheme(
    /* ─── primarios ─── */
    primary            = primaryDark,
    onPrimary          = onPrimaryDark,
    primaryContainer   = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    inversePrimary     = inversePrimaryDark,

    /* ─── secundarios / terciarios ─── */
    secondary            = secondaryDark,
    onSecondary          = onSecondaryDark,
    secondaryContainer   = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary             = tertiaryDark,
    onTertiary           = onTertiaryDark,
    tertiaryContainer    = tertiaryContainerDark,
    onTertiaryContainer  = onTertiaryContainerDark,

    /* ─── superficies y fondo ─── */
    background         = backgroundDark,
    onBackground       = onBackgroundDark,
    surface            = surfaceDark,
    onSurface          = onSurfaceDark,
    surfaceVariant     = surfaceVariantDark,
    onSurfaceVariant   = onSurfaceVariantDark,
    surfaceTint        = surfaceTintDark,
    inverseSurface     = inverseSurfaceDark,
    inverseOnSurface   = inverseOnSurfaceDark,

    /* ─── feedback ─── */
    error            = errorDark,
    onError          = onErrorDark,
    errorContainer   = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    outline          = outlineDark,
    outlineVariant   = outlineVariantDark,
    scrim            = scrimDark,

    /* ─── superficies jerárquicas ─── */
    surfaceBright           = surfaceBrightDark,
    surfaceContainer        = surfaceContainerDark,
    surfaceContainerLow     = surfaceContainerLowDark,
    surfaceContainerLowest  = surfaceContainerLowestDark,
    surfaceContainerHigh    = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
    surfaceDim              = surfaceDimDark
)

/**
 * Paleta clara – roles Material 3.
 */
private val LightColorScheme = lightColorScheme(
    /* ─── primarios ─── */
    primary            = primaryLight,
    onPrimary          = onPrimaryLight,
    primaryContainer   = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    inversePrimary     = inversePrimaryLight,

    /* ─── secundarios / terciarios ─── */
    secondary            = secondaryLight,
    onSecondary          = onSecondaryLight,
    secondaryContainer   = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary             = tertiaryLight,
    onTertiary           = onTertiaryLight,
    tertiaryContainer    = tertiaryContainerLight,
    onTertiaryContainer  = onTertiaryContainerLight,

    /* ─── superficies y fondo ─── */
    background         = backgroundLight,
    onBackground       = onBackgroundLight,
    surface            = surfaceLight,
    onSurface          = onSurfaceLight,
    surfaceVariant     = surfaceVariantLight,
    onSurfaceVariant   = onSurfaceVariantLight,
    surfaceTint        = surfaceTintLight,
    inverseSurface     = inverseSurfaceLight,
    inverseOnSurface   = inverseOnSurfaceLight,

    /* ─── feedback ─── */
    error            = errorLight,
    onError          = onErrorLight,
    errorContainer   = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    outline          = outlineLight,
    outlineVariant   = outlineVariantLight,
    scrim            = scrimLight,

    /* ─── superficies jerárquicas ─── */
    surfaceBright           = surfaceBrightLight,
    surfaceContainer        = surfaceContainerLight,
    surfaceContainerLow     = surfaceContainerLowLight,
    surfaceContainerLowest  = surfaceContainerLowestLight,
    surfaceContainerHigh    = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
    surfaceDim              = surfaceDimLight
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

